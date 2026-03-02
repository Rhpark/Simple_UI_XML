# Android Code Review Examples

Real-world examples of common issues and recommended patterns for Android Kotlin development.

## Example 1: Memory Leak - Activity Reference in ViewModel

### ❌ BAD: Holding Activity Reference
```kotlin
// PaymentViewModel.kt
class PaymentViewModelPayoo
    private val activity: PaymentActivity // Memory leak!
) : ViewModel() {

    fun onPaymentComplete() {
        activity.showSuccessDialog() // Crash if Activity destroyed!
        activity.finish()
    }
}

// PaymentActivity.kt
class PaymentActivity : AppCompatActivity() {

    private val viewModel by viewModels<PaymentViewModel> {
        PaymentViewModelFactory(this) // Passing Activity reference!
    }
}
```

**Issues:**
- 🔴 **Critical**: Memory leak - ViewModel outlives Activity
- 🔴 **Critical**: Crash risk if Activity destroyed but ViewModel still active
- 🟠 **High**: Tight coupling between ViewModel and View

### ✅ GOOD: Event-Based Navigation
```kotlin
// PaymentViewModel.kt
class PaymentViewModel : ViewModel() {

    private val _events = MutableSharedFlow<PaymentEvent>()
    val events: SharedFlow<PaymentEvent> = _events.asSharedFlow()

    fun onPaymentComplete() {
        viewModelScope.launch {
            _events.emit(PaymentEvent.ShowSuccessDialog)
            _events.emit(PaymentEvent.Finish)
        }
    }
}

sealed class PaymentEvent {
    object ShowSuccessDialog : PaymentEvent()
    object Finish : PaymentEvent()
    data class NavigateTo(val destination: String) : PaymentEvent()
}

// PaymentActivity.kt
@AndroidEntryPoint
class PaymentActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModel: PaymentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    PaymentEvent.ShowSuccessDialog -> showSuccessDialog()
                    PaymentEvent.Finish -> finish()
                    is PaymentEvent.NavigateTo -> navigate(event.destination)
                }
            }
        }
    }
}
```

**Benefits:**
- ✅ No memory leaks - ViewModel doesn't hold Activity reference
- ✅ Lifecycle-safe - Events only processed when Activity is active
- ✅ Testable - ViewModel logic can be tested independently

---

## Example 2: Coroutine Scope and Cancellation

### ❌ BAD: GlobalScope and No Cancellation
```kotlin
class PaymentViewModel : ViewModel() {

    fun loadPayments() {
        GlobalScope.launch { // Never cancelled!
            val payments = repository.getPayments()
            _payments.value = payments // Can crash if ViewModel cleared
        }
    }

    fun processPayment(request: PaymentRequest) {
        GlobalScope.launch(Dispatchers.Main) {
            val result = repository.processPayment(request) // Blocks UI thread!
            _result.value = result
        }
    }
}
```

**Issues:**
- 🔴 **Critical**: GlobalScope coroutines never cancelled - resource leak
- 🔴 **Critical**: Network/DB on Main thread - ANR risk
- 🟠 **High**: Crash risk when updating UI after ViewModel cleared

### ✅ GOOD: ViewModelScope and Proper Dispatchers
```kotlin
class PaymentViewModel(
    private val repository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Payment>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Payment>>> = _uiState.asStateFlow()

    fun loadPayments() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                // Repository handles IO dispatcher internally
                val payments = repository.getPayments()
                _uiState.value = UiState.Success(payments)
            } catch (e: IOException) {
                _uiState.value = UiState.Error("Network error: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Unexpected error: ${e.message}")
            }
        }
    }

    fun processPayment(request: PaymentRequest) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            runCatching {
                repository.processPayment(request)
            }.onSuccess { result ->
                _uiState.value = UiState.Success(result)
            }.onFailure { error ->
                _uiState.value = UiState.Error(error.message ?: "Unknown error")
            }
        }
    }
}

// Repository with proper dispatcher
class PaymentRepositoryImpl(
    private val api: PaymentApi,
    private val dao: PaymentDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PaymentRepository {

    override suspend fun getPayments(): List<Payment> = withContext(ioDispatcher) {
        api.fetchPayments()
    }

    override suspend fun processPayment(request: PaymentRequest): PaymentResult =
        withContext(ioDispatcher) {
            api.processPayment(request)
        }
}
```

**Benefits:**
- ✅ Automatic cancellation when ViewModel cleared
- ✅ Proper thread management - IO work on background
- ✅ Comprehensive error handling
- ✅ Single source of truth for UI state

---

## Example 3: Clean Architecture Violation

### ❌ BAD: ViewModel with Business Logic and Direct API Calls
```kotlin
class PaymentViewModel(
    private val api: PaymentApi // Wrong layer!
) : ViewModel() {

    private val _result = MutableLiveData<PaymentResult>()
    val result: LiveData<PaymentResult> = _result

    fun processPayment(amount: String, merchantId: String) {
        viewModelScope.launch {
            // Business logic in ViewModel - WRONG!
            val amountBigDecimal = try {
                BigDecimal(amount)
            } catch (e: NumberFormatException) {
                _result.value = PaymentResult.Error("Invalid amount")
                return@launch
            }

            if (amountBigDecimal < BigDecimal("1000")) {
                _result.value = PaymentResult.Error("Minimum amount is 1000 VND")
                return@launch
            }

            val fee = amountBigDecimal * BigDecimal("0.02") // Business logic!
            val total = amountBigDecimal + fee

            // Calling API directly - WRONG!
            try {
                val response = api.processPayment(
                    PaymentRequest(
                        amount = total,
                        merchantId = merchantId,
                        timestamp = System.currentTimeMillis()
                    )
                )
                _result.value = PaymentResult.Success(response)
            } catch (e: Exception) {
                _result.value = PaymentResult.Error(e.message ?: "Error")
            }
        }
    }
}
```

**Issues:**
- 🟠 **High**: Business logic in ViewModel (validation, fee calculation)
- 🟠 **High**: ViewModel depends on Data layer (API) directly
- 🟡 **Medium**: No UseCase - business logic not reusable
- 🟡 **Medium**: Mixing concerns - validation, calculation, networking

### ✅ GOOD: Proper Clean Architecture Layers
```kotlin
// Domain Layer - UseCase
class ProcessPaymentUseCase(
    private val repository: PaymentRepository,
    private val validator: PaymentValidator,
    private val feeCalculator: FeeCalculator
) {
    suspend operator fun invoke(
        amount: String,
        merchantId: String
    ): Result<PaymentResult> = runCatching {
        // Business logic in UseCase
        val amountBigDecimal = validator.parseAndValidateAmount(amount)
        validator.validateMerchant(merchantId)

        val fee = feeCalculator.calculateFee(amountBigDecimal)
        val total = amountBigDecimal + fee

        val request = PaymentRequest(
            amount = total,
            merchantId = merchantId,
            timestamp = System.currentTimeMillis()
        )

        repository.processPayment(request)
    }
}

// Domain Layer - Validators and Calculators
class PaymentValidator {
    fun parseAndValidateAmount(amount: String): BigDecimal {
        val amountBigDecimal = amount.toBigDecimalOrNull()
            ?: throw IllegalArgumentException("Invalid amount format")

        if (amountBigDecimal < MIN_AMOUNT) {
            throw IllegalArgumentException("Minimum amount is $MIN_AMOUNT VND")
        }

        return amountBigDecimal
    }

    fun validateMerchant(merchantId: String) {
        if (merchantId.isBlank()) {
            throw IllegalArgumentException("Invalid merchant ID")
        }
    }

    companion object {
        private val MIN_AMOUNT = BigDecimal("1000")
    }
}

class FeeCalculator {
    fun calculateFee(amount: BigDecimal): BigDecimal {
        return amount * FEE_RATE
    }

    companion object {
        private val FEE_RATE = BigDecimal("0.02")
    }
}

// Presentation Layer - ViewModel
class PaymentViewModel(
    private val processPaymentUseCase: ProcessPaymentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Initial)
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun processPayment(amount: String, merchantId: String) {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading

            processPaymentUseCase(amount, merchantId)
                .onSuccess { result ->
                    _uiState.value = PaymentUiState.Success(result)
                }
                .onFailure { error ->
                    _uiState.value = PaymentUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}

// Data Layer - Repository
class PaymentRepositoryImpl(
    private val remoteDataSource: PaymentRemoteDataSource,
    private val localDataSource: PaymentLocalDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PaymentRepository {

    override suspend fun processPayment(
        request: PaymentRequest
    ): PaymentResult = withContext(ioDispatcher) {
        val result = remoteDataSource.processPayment(request)
        localDataSource.savePayment(result.toEntity())
        result
    }
}
```

**Benefits:**
- ✅ Clear separation of concerns
- ✅ Business logic in Domain layer (UseCase)
- ✅ ViewModel only handles presentation logic
- ✅ Reusable business logic
- ✅ Testable - each layer can be tested independently

---

## Example 4: Insecure Storage

### ❌ BAD: Plain SharedPreferences for Sensitive Data
```kotlin
class AuthManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        prefs.edit {
            putString("auth_token", token) // Plain text!
        }
    }

    fun saveUserPin(pin: String) {
        prefs.edit {
            putString("user_pin", pin) // Plain text!
        }
    }

    fun getAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }
}
```

**Issues:**
- 🔴 **Critical**: Auth token stored in plain text
- 🔴 **Critical**: User PIN stored in plain text
- 🔴 **Critical**: Security vulnerability - data can be read by rooted devices

### ✅ GOOD: EncryptedSharedPreferences
```kotlin
class SecureAuthManager(private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_auth",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAuthToken(token: String) {
        encryptedPrefs.edit {
            putString(KEY_AUTH_TOKEN, token)
        }
    }

    fun saveUserPin(pin: String) {
        encryptedPrefs.edit {
            putString(KEY_USER_PIN, pin)
        }
    }

    fun getAuthToken(): String? {
        return encryptedPrefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun clearAuthData() {
        encryptedPrefs.edit {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER_PIN)
        }
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_PIN = "user_pin"
    }
}
```

**Benefits:**
- ✅ Encrypted storage using AES256
- ✅ Protected against unauthorized access
- ✅ Secure even on rooted devices
- ✅ Clear API for data management

---

## Example 5: Inefficient RecyclerView

### ❌ BAD: notifyDataSetChanged and No ViewBinding
```kotlin
class PaymentAdapter : RecyclerView.Adapter<PaymentAdapter.ViewHolder>() {

    private var payments: List<Payment> = emptyList()

    fun updatePayments(newPayments: List<Payment>) {
        payments = newPayments
        notifyDataSetChanged() // Inefficient!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(payments[position])
    }

    override fun getItemCount() = payments.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Finding views repeatedly - inefficient!
        fun bind(payment: Payment) {
            itemView.findViewById<TextView>(R.id.amountTextView).text =
                payment.amount.toString()
            itemView.findViewById<TextView>(R.id.merchantTextView).text =
                payment.merchantName
            itemView.findViewById<TextView>(R.id.dateTextView).text =
                payment.formattedDate
        }
    }
}
```

**Issues:**
- 🟡 **Medium**: notifyDataSetChanged() redraws entire list
- 🟡 **Medium**: findViewById called repeatedly in bind()
- 🟡 **Medium**: No ViewBinding - prone to errors

### ✅ GOOD: ListAdapter with DiffUtil and ViewBinding
```kotlin
class PaymentAdapter(
    private val onItemClick: (Payment) -> Unit
) : ListAdapter<Payment, PaymentAdapter.ViewHolder>(PaymentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPaymentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemPaymentBinding,
        private val onItemClick: (Payment) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(payment: Payment) {
            binding.apply {
                amountTextView.text = payment.amount.formatAsCurrency()
                merchantTextView.text = payment.merchantName
                dateTextView.text = payment.formattedDate
                statusBadge.text = payment.status

                // Set status color
                statusBadge.setBackgroundResource(
                    when (payment.status) {
                        "SUCCESS" -> R.drawable.bg_status_success
                        "PENDING" -> R.drawable.bg_status_pending
                        "FAILED" -> R.drawable.bg_status_failed
                        else -> R.drawable.bg_status_unknown
                    }
                )

                root.setOnClickListener {
                    onItemClick(payment)
                }
            }
        }
    }
}

class PaymentDiffCallback : DiffUtil.ItemCallback<Payment>() {
    override fun areItemsTheSame(oldItem: Payment, newItem: Payment): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Payment, newItem: Payment): Boolean {
        return oldItem == newItem
    }
}

// Usage in Fragment
class PaymentListFragment : Fragment() {

    private val adapter = PaymentAdapter { payment ->
        navigateToPaymentDetail(payment.id)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.payments.collect { payments ->
                adapter.submitList(payments) // DiffUtil automatically calculates changes
            }
        }
    }
}
```

**Benefits:**
- ✅ Efficient updates - only changed items redrawn
- ✅ ViewBinding - type-safe, no findViewById
- ✅ Automatic animation with DiffUtil
- ✅ Click handling with lambda
- ✅ Extension function for formatting

---

## Example 6: Fragment Lifecycle Issues

### ❌ BAD: Wrong Lifecycle Owner
```kotlin
class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Using fragment's lifecycle - WRONG!
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUi(state) // Can crash after onDestroyView!
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

**Issues:**
- 🔴 **Critical**: Using fragment lifecycle instead of view lifecycle
- 🔴 **Critical**: Crash risk - accessing binding after onDestroyView
- 🟠 **High**: Memory leak between onDestroyView and onDestroy

### ✅ GOOD: Proper Lifecycle Management
```kotlin
class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaymentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.submitButton.setOnClickListener {
            val amount = binding.amountEditText.text.toString()
            viewModel.processPayment(amount)
        }
    }

    private fun observeViewModel() {
        // Use viewLifecycleOwner for UI updates
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUi(state)
            }
        }

        // Separate collection for one-time events
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collect { event ->
                handleEvent(event)
            }
        }
    }

    private fun updateUi(state: PaymentUiState) {
        when (state) {
            is PaymentUiState.Loading -> {
                binding.progressBar.show()
                binding.submitButton.isEnabled = false
            }
            is PaymentUiState.Success -> {
                binding.progressBar.hide()
                binding.submitButton.isEnabled = true
                showSuccessMessage(state.result)
            }
            is PaymentUiState.Error -> {
                binding.progressBar.hide()
                binding.submitButton.isEnabled = true
                showError(state.message)
            }
            is PaymentUiState.Initial -> {
                binding.progressBar.hide()
                binding.submitButton.isEnabled = true
            }
        }
    }

    private fun handleEvent(event: PaymentEvent) {
        when (event) {
            is PaymentEvent.NavigateToReceipt -> {
                findNavController().navigate(
                    PaymentFragmentDirections.actionToReceipt(event.transactionId)
                )
            }
            is PaymentEvent.ShowError -> {
                Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

**Benefits:**
- ✅ Correct lifecycle owner - viewLifecycleOwner
- ✅ No crash risk - collections cancelled when view destroyed
- ✅ Proper binding cleanup
- ✅ Separation of state and events
- ✅ Clean code organization

---

## Example 7: Hardcoded Configuration

### ❌ BAD: Hardcoded API Keys and URLs
```kotlin
class ApiClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.payoo.vn/v1/") // Hardcoded!
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiKey = "sk_live_abc123xyz789" // Hardcoded secret!

    fun getPaymentApi(): PaymentApi {
        return retrofit.create(PaymentApi::class.java)
    }
}

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("API-Key", "sk_live_abc123xyz789") // Hardcoded!
            .build()
        return chain.proceed(request)
    }
}
```

**Issues:**
- 🔴 **Critical**: API key exposed in source code
- 🟠 **High**: Can't switch between dev/staging/prod
- 🟠 **High**: Security risk if code is decompiled

### ✅ GOOD: BuildConfig and Gradle Properties
```kotlin
// gradle.properties (add to .gitignore)
API_KEY_DEBUG=sk_test_debug_key
API_KEY_RELEASE=sk_live_production_key
API_BASE_URL_DEBUG=https://api-dev.payoo.vn/v1/
API_BASE_URL_RELEASE=https://api.payoo.vn/v1/

// app/build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "API_KEY", "\"${project.findProperty("API_KEY_DEBUG")}\"")
        buildConfigField("String", "API_BASE_URL", "\"${project.findProperty("API_BASE_URL_DEBUG")}\"")
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "API_KEY", "\"${project.findProperty("API_KEY_DEBUG")}\"")
            buildConfigField("String", "API_BASE_URL", "\"${project.findProperty("API_BASE_URL_DEBUG")}\"")
        }
        getByName("release") {
            buildConfigField("String", "API_KEY", "\"${project.findProperty("API_KEY_RELEASE")}\"")
            buildConfigField("String", "API_BASE_URL", "\"${project.findProperty("API_BASE_URL_RELEASE")}\"")
        }
    }
}

// NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthInterceptor(): AuthInterceptor {
        return AuthInterceptor(BuildConfig.API_KEY)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

class AuthInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("API-Key", apiKey)
            .addHeader("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}
```

**Benefits:**
- ✅ API keys not in source code
- ✅ Different configs for debug/release
- ✅ Secure - keys in gradle.properties (gitignored)
- ✅ Easy to manage different environments
- ✅ Dependency injection for testability

---

## Summary of Common Issues

### Critical (Fix Immediately)
1. Memory leaks (Activity/Context in ViewModel)
2. Coroutines not cancelled (GlobalScope)
3. Plain text storage for sensitive data
4. Hardcoded API keys/secrets
5. UI updates on background thread

### High Priority (Fix Soon)
6. Business logic in ViewModel
7. Direct API calls from ViewModel
8. Wrong lifecycle owner in Fragments
9. No error handling in coroutines
10. Wrong Dispatcher usage

### Medium Priority (Should Improve)
11. notifyDataSetChanged() instead of DiffUtil
12. findViewById instead of ViewBinding
13. No null safety checks
14. Poor naming conventions
15. Not using data classes

These examples provide concrete guidance for identifying and fixing common Android code issues during reviews.
