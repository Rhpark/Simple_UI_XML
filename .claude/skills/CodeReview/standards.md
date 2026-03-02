# Android Code Review Standards

Comprehensive standards for Android Kotlin development in the Payoo Android application.

## 1. Naming Conventions

### Types and Classes
```kotlin
// ✅ GOOD: PascalCase, descriptive
class PaymentViewModel
class TransactionRepository
interface UserDataSource
sealed class PaymentResult

// ❌ BAD: Abbreviations, unclear
class PmtVM
class TxnRepo
```

### Variables and Properties
```kotlin
// ✅ GOOD: camelCase, descriptive
val paymentAmount: BigDecimal
var isLoading: Boolean
private val transactionList: List<Transaction>

// ❌ BAD: Abbreviations, unclear
val amt: BigDecimal
var loading: Boolean
val txns: List<Transaction>
```

### Constants
```kotlin
// ✅ GOOD: UPPER_SNAKE_CASE
const val MAX_RETRY_COUNT = 3
const val API_BASE_URL = "https://api.payoo.vn"
private const val CACHE_DURATION_MS = 5000L

// ❌ BAD: Wrong case
const val maxRetryCount = 3
const val apiBaseUrl = "https://api.payoo.vn"
```

### Boolean Variables
```kotlin
// ✅ GOOD: Prefix with is, has, should, can
val isPaymentSuccessful: Boolean
val hasInternetConnection: Boolean
val shouldRetry: Boolean
val canProceed: Boolean

// ❌ BAD: No prefix
val paymentSuccessful: Boolean
val internetConnection: Boolean
```

### View IDs and Binding
```kotlin
// ✅ GOOD: Include type suffix
binding.amountEditText
binding.submitButton
binding.paymentRecyclerView
binding.errorTextView

// ❌ BAD: No type suffix
binding.amount
binding.submit
binding.payments
```

## 2. Kotlin Best Practices

### Null Safety
```kotlin
// ✅ GOOD: Safe calls and Elvis operator
val length = text?.length ?: 0
user?.name?.let { name ->
    displayName(name)
}

// ❌ BAD: Force unwrap without checking
val length = text!!.length
displayName(user!!.name!!)
```

### Data Classes
```kotlin
// ✅ GOOD: Data classes for DTOs/Models
data class User(
    val id: String,
    val name: String,
    val email: String
)

data class PaymentRequest(
    val amount: BigDecimal,
    val merchantId: String,
    val currency: String = "VND"
)

// ❌ BAD: Regular class for simple data
class User {
    var id: String = ""
    var name: String = ""
    var email: String = ""
}
```

### Sealed Classes for State
```kotlin
// ✅ GOOD: Sealed classes for state management
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

sealed class PaymentResult {
    data class Success(val transactionId: String) : PaymentResult()
    data class Failure(val error: PaymentError) : PaymentResult()
    object Cancelled : PaymentResult()
}

// ❌ BAD: Using enums or nullable types
enum class State { LOADING, SUCCESS, ERROR }
```

### Immutability
```kotlin
// ✅ GOOD: Prefer val over var
val userId = getUserId()
val configuration = Config(apiKey, baseUrl)

// ❌ BAD: Unnecessary var
var userId = getUserId() // Never changed
var configuration = Config(apiKey, baseUrl) // Never changed
```

### Extension Functions
```kotlin
// ✅ GOOD: Extension functions for reusable utilities
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun BigDecimal.formatAsCurrency(): String {
    return NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(this)
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

// Usage
if (email.isValidEmail()) {
    binding.submitButton.show()
}
```

### Scope Functions
```kotlin
// ✅ GOOD: Appropriate scope function usage

// apply: Configure object
val user = User().apply {
    name = "John"
    email = "john@example.com"
}

// let: Null-safe operations
user?.let { u ->
    saveUser(u)
}

// also: Side effects
val numbers = mutableListOf(1, 2, 3).also {
    println("List created with ${it.size} elements")
}

// run: Execute block and return result
val result = repository.run {
    fetchData()
    processData()
}

// with: Multiple operations on object
with(binding) {
    titleTextView.text = title
    descriptionTextView.text = description
    imageView.load(imageUrl)
}
```

## 3. Coroutines Patterns

### Proper Scope Usage
```kotlin
// ✅ GOOD: Use appropriate scope
class PaymentViewModel : ViewModel() {
    fun loadPayments() {
        viewModelScope.launch {
            // Automatically cancelled when ViewModel cleared
            val payments = repository.getPayments()
            _uiState.value = UiState.Success(payments)
        }
    }
}

class PaymentFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            // Cancelled when view is destroyed
            viewModel.uiState.collect { state ->
                updateUi(state)
            }
        }
    }
}

// ❌ BAD: GlobalScope or runBlocking
GlobalScope.launch { // Never cancelled
    repository.getPayments()
}

runBlocking { // Blocks thread
    repository.getPayments()
}
```

### Dispatcher Usage
```kotlin
// ✅ GOOD: Appropriate dispatcher for task
class PaymentRepository(
    private val api: PaymentApi,
    private val database: PaymentDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun getPayments(): List<Payment> = withContext(ioDispatcher) {
        val remotePayments = api.fetchPayments()
        database.insertAll(remotePayments)
        database.getAllPayments()
    }
}

// ❌ BAD: Wrong dispatcher
class PaymentRepository {
    suspend fun getPayments(): List<Payment> = withContext(Dispatchers.Main) {
        // Network/DB work on Main thread!
        api.fetchPayments()
    }
}
```

### Error Handling
```kotlin
// ✅ GOOD: Proper error handling
viewModelScope.launch {
    _uiState.value = UiState.Loading

    try {
        val result = repository.processPayment(request)
        _uiState.value = UiState.Success(result)
    } catch (e: IOException) {
        _uiState.value = UiState.Error("Network error: ${e.message}")
    } catch (e: Exception) {
        _uiState.value = UiState.Error("Unexpected error: ${e.message}")
    }
}

// Or using runCatching
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

// ❌ BAD: No error handling
viewModelScope.launch {
    val result = repository.processPayment(request) // Can crash
    _uiState.value = UiState.Success(result)
}
```

### Flow Usage
```kotlin
// ✅ GOOD: StateFlow for UI state
class PaymentViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<Payment>>(UiState.Loading)
    val uiState: StateFlow<UiState<Payment>> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PaymentEvent>()
    val events: SharedFlow<PaymentEvent> = _events.asSharedFlow()

    fun processPayment(request: PaymentRequest) {
        viewModelScope.launch {
            repository.processPayment(request)
                .catch { error ->
                    _uiState.value = UiState.Error(error.message)
                }
                .collect { result ->
                    _uiState.value = UiState.Success(result)
                }
        }
    }
}

// ❌ BAD: LiveData with nullable types
class PaymentViewModel : ViewModel() {
    val payment: MutableLiveData<Payment?> = MutableLiveData(null)
    val error: MutableLiveData<String?> = MutableLiveData(null)
}
```

## 4. Clean Architecture

### Layer Structure
```
app/
├── data/
│   ├── datasource/    # API, Database, Cache
│   ├── repository/    # Implementation
│   └── model/         # DTOs, Entities
├── domain/
│   ├── model/         # Domain models
│   ├── repository/    # Repository interfaces
│   └── usecase/       # Business logic
└── presentation/
    ├── ui/            # Activities, Fragments
    ├── viewmodel/     # ViewModels
    └── adapter/       # RecyclerView adapters
```

### ViewModel (Presentation Layer)
```kotlin
// ✅ GOOD: ViewModel uses UseCase, exposes UI state
class PaymentViewModel(
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val getPaymentHistoryUseCase: GetPaymentHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Initial)
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun processPayment(amount: BigDecimal, merchantId: String) {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading

            processPaymentUseCase(amount, merchantId)
                .onSuccess { result ->
                    _uiState.value = PaymentUiState.Success(result)
                }
                .onFailure { error ->
                    _uiState.value = PaymentUiState.Error(error.message)
                }
        }
    }
}

// ❌ BAD: ViewModel calls repository directly, has business logic
class PaymentViewModel(
    private val repository: PaymentRepository
) : ViewModel() {

    fun processPayment(amount: BigDecimal, merchantId: String) {
        viewModelScope.launch {
            // Business logic in ViewModel - WRONG!
            if (amount < BigDecimal.ZERO) {
                _error.value = "Invalid amount"
                return@launch
            }

            val fee = amount * BigDecimal("0.02") // Business logic!
            val total = amount + fee

            // Calling repository directly - WRONG!
            repository.processPayment(total, merchantId)
        }
    }
}
```

### UseCase (Domain Layer)
```kotlin
// ✅ GOOD: UseCase contains business logic
class ProcessPaymentUseCase(
    private val paymentRepository: PaymentRepository,
    private val feeCalculator: FeeCalculator,
    private val validator: PaymentValidator
) {
    suspend operator fun invoke(
        amount: BigDecimal,
        merchantId: String
    ): Result<PaymentResult> = runCatching {
        // Business logic here
        validator.validateAmount(amount)
        validator.validateMerchant(merchantId)

        val fee = feeCalculator.calculateFee(amount)
        val totalAmount = amount + fee

        val request = PaymentRequest(
            amount = totalAmount,
            merchantId = merchantId,
            timestamp = System.currentTimeMillis()
        )

        paymentRepository.processPayment(request)
    }
}

// ❌ BAD: UseCase just forwards to repository
class ProcessPaymentUseCase(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(request: PaymentRequest) =
        repository.processPayment(request) // No value added!
}
```

### Repository (Data Layer)
```kotlin
// ✅ GOOD: Repository abstracts data sources
interface PaymentRepository {
    suspend fun processPayment(request: PaymentRequest): PaymentResult
    suspend fun getPaymentHistory(): List<Payment>
}

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

    override suspend fun getPaymentHistory(): List<Payment> = withContext(ioDispatcher) {
        try {
            val remote = remoteDataSource.getPayments()
            localDataSource.saveAll(remote.map { it.toEntity() })
            remote
        } catch (e: IOException) {
            // Fallback to cache
            localDataSource.getPayments().map { it.toDomain() }
        }
    }
}

// ❌ BAD: Repository has business logic
class PaymentRepositoryImpl(
    private val api: PaymentApi
) : PaymentRepository {

    override suspend fun processPayment(request: PaymentRequest): PaymentResult {
        // Business logic in repository - WRONG!
        if (request.amount < MIN_AMOUNT) {
            throw IllegalArgumentException("Amount too low")
        }

        val fee = request.amount * FEE_RATE // Business logic!
        val modifiedRequest = request.copy(amount = request.amount + fee)

        return api.processPayment(modifiedRequest)
    }
}
```

## 5. Lifecycle Management

### Activity/Fragment References
```kotlin
// ✅ GOOD: No Activity/Fragment references in ViewModel
class PaymentViewModel(
    private val processPaymentUseCase: ProcessPaymentUseCase
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    fun onPaymentSuccess() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.ToReceipt)
        }
    }
}

// In Fragment
class PaymentFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    NavigationEvent.ToReceipt -> navigateToReceipt()
                }
            }
        }
    }
}

// ❌ BAD: Holding Activity/Fragment reference
class PaymentViewModel(
    private val fragment: PaymentFragment // Memory leak!
) : ViewModel() {

    fun onPaymentSuccess() {
        fragment.navigateToReceipt() // Crash if Fragment destroyed!
    }
}
```

### Observer Lifecycle
```kotlin
// ✅ GOOD: Use viewLifecycleOwner in Fragments
class PaymentFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use viewLifecycleOwner, not this (fragment lifecycle)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUi(state)
            }
        }
    }
}

// ❌ BAD: Using fragment's lifecycle
class PaymentFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // This leaks between onDestroyView and onDestroy!
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUi(state) // Can crash if view is destroyed
            }
        }
    }
}
```

### Cleanup
```kotlin
// ✅ GOOD: Proper cleanup
class PaymentViewModel : ViewModel() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}

// ❌ BAD: No cleanup
class PaymentViewModel : ViewModel() {

    private val scope = CoroutineScope(Dispatchers.IO)

    // No cleanup - scope keeps running!
}
```

## 6. Security Best Practices

### Secure Storage
```kotlin
// ✅ GOOD: Encrypted storage for sensitive data
class SecurePreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        encryptedPrefs.edit {
            putString(KEY_AUTH_TOKEN, token)
        }
    }
}

// ❌ BAD: Plain SharedPreferences for sensitive data
class Preferences(context: Context) {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit {
            putString("auth_token", token) // Plain text!
        }
    }
}
```

### API Keys
```kotlin
// ✅ GOOD: API keys in gradle.properties (not checked in) and BuildConfig
// gradle.properties (add to .gitignore)
API_KEY=your_secret_key_here
API_BASE_URL=https://api.payoo.vn

// build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "API_KEY", "\"${project.findProperty("API_KEY")}\"")
        buildConfigField("String", "API_BASE_URL", "\"${project.findProperty("API_BASE_URL")}\"")
    }
}

// Usage
class ApiClient {
    private val apiKey = BuildConfig.API_KEY
    private val baseUrl = BuildConfig.API_BASE_URL
}

// ❌ BAD: Hardcoded API keys
class ApiClient {
    private val apiKey = "sk_live_1234567890abcdef" // Exposed in code!
    private val baseUrl = "https://api.payoo.vn"
}
```

### Logging
```kotlin
// ✅ GOOD: No sensitive data in logs, conditional logging
class PaymentLogger {

    fun logPayment(request: PaymentRequest) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Processing payment for merchant: ${request.merchantId}")
            Log.d(TAG, "Amount: ${request.amount.setScale(2)}")
            // NO card numbers, tokens, or PII
        }
    }
}

// ❌ BAD: Logging sensitive data
class PaymentLogger {

    fun logPayment(request: PaymentRequest) {
        Log.d(TAG, "Payment: $request") // Might contain sensitive data!
        Log.d(TAG, "Token: ${request.authToken}") // Exposed in logs!
        Log.d(TAG, "Card: ${request.cardNumber}") // PCI violation!
    }
}
```

## 7. Performance Optimization

### Background Work
```kotlin
// ✅ GOOD: Network/DB on background thread
class PaymentRepository(
    private val api: PaymentApi,
    private val dao: PaymentDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun syncPayments() = withContext(ioDispatcher) {
        val remotePayments = api.fetchPayments()
        dao.deleteAll()
        dao.insertAll(remotePayments.map { it.toEntity() })
    }
}

// ❌ BAD: Blocking Main thread
class PaymentRepository(
    private val api: PaymentApi,
    private val dao: PaymentDao
) {

    fun syncPayments() {
        // Blocking Main thread!
        val remotePayments = api.fetchPayments().execute()
        dao.deleteAll()
        dao.insertAll(remotePayments.map { it.toEntity() })
    }
}
```

### RecyclerView Optimization
```kotlin
// ✅ GOOD: DiffUtil for efficient updates
class PaymentAdapter : ListAdapter<Payment, PaymentViewHolder>(PaymentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPaymentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PaymentDiffCallback : DiffUtil.ItemCallback<Payment>() {
    override fun areItemsTheSame(oldItem: Payment, newItem: Payment) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Payment, newItem: Payment) =
        oldItem == newItem
}

// ❌ BAD: notifyDataSetChanged for all updates
class PaymentAdapter : RecyclerView.Adapter<PaymentViewHolder>() {

    private var payments: List<Payment> = emptyList()

    fun updatePayments(newPayments: List<Payment>) {
        payments = newPayments
        notifyDataSetChanged() // Inefficient!
    }
}
```

### Image Loading
```kotlin
// ✅ GOOD: Coil/Glide with proper caching
class ImageLoader(private val context: Context) {

    fun loadImage(imageView: ImageView, url: String) {
        imageView.load(url) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.error)
            transformations(CircleCropTransformation())
            memoryCachePolicy(CachePolicy.ENABLED)
            diskCachePolicy(CachePolicy.ENABLED)
        }
    }
}

// ❌ BAD: Loading images without caching
class ImageLoader {

    suspend fun loadImage(imageView: ImageView, url: String) {
        val bitmap = withContext(Dispatchers.IO) {
            URL(url).openStream().use { stream ->
                BitmapFactory.decodeStream(stream) // No caching, inefficient!
            }
        }
        imageView.setImageBitmap(bitmap)
    }
}
```

### Database Optimization
```kotlin
// ✅ GOOD: Indexed queries, efficient types
@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["merchantId"]),
        Index(value = ["timestamp"])
    ]
)
data class PaymentEntity(
    @PrimaryKey val id: String,
    val merchantId: String,
    val amount: Long, // Store cents, not BigDecimal
    val timestamp: Long,
    val status: String
)

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments WHERE merchantId = :merchantId AND timestamp > :after ORDER BY timestamp DESC")
    fun getPaymentsByMerchant(merchantId: String, after: Long): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<PaymentEntity>)
}

// ❌ BAD: No indexes, inefficient queries
@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val merchantId: String,
    val amount: BigDecimal, // Not supported by Room!
    val timestamp: Long,
    val status: String
)

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments")
    fun getAllPayments(): List<PaymentEntity> // Load all, then filter!
}
```

## 8. Dependency Injection

### Hilt/Dagger Setup
```kotlin
// ✅ GOOD: Proper DI with Hilt
@HiltAndroidApp
class PayooApplication : Application()

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
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

    @Provides
    @Singleton
    fun providePaymentApi(retrofit: Retrofit): PaymentApi {
        return retrofit.create(PaymentApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(
        impl: PaymentRepositoryImpl
    ): PaymentRepository
}

@AndroidEntryPoint
class PaymentActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: PaymentViewModelFactory
}

// ❌ BAD: Manual instantiation, singletons
object ApiClient {
    val instance: PaymentApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.payoo.vn")
            .build()
            .create(PaymentApi::class.java)
    }
}

class PaymentViewModel {
    private val repository = PaymentRepository(ApiClient.instance) // Tight coupling!
}
```

---

## Summary Checklist

Use this checklist during code reviews:

### Naming ✅
- [ ] Classes/Interfaces in PascalCase
- [ ] Variables/Functions in camelCase
- [ ] Constants in UPPER_SNAKE_CASE
- [ ] Boolean variables prefixed (is, has, should, can)
- [ ] No unclear abbreviations

### Kotlin ✅
- [ ] Null safety properly handled
- [ ] Data classes for DTOs/models
- [ ] Sealed classes for states
- [ ] Prefer val over var
- [ ] Extension functions where appropriate

### Coroutines ✅
- [ ] Proper scope usage (viewModelScope, lifecycleScope)
- [ ] Correct dispatchers (IO for network/DB, Main for UI)
- [ ] Error handling in all coroutines
- [ ] Flows for reactive data
- [ ] No runBlocking in production

### Architecture ✅
- [ ] Clear layer separation (Presentation/Domain/Data)
- [ ] ViewModels use UseCases
- [ ] UseCases contain business logic
- [ ] Repositories abstract data sources
- [ ] Dependency injection configured

### Lifecycle ✅
- [ ] No Activity/Fragment references in ViewModel
- [ ] viewLifecycleOwner in Fragments
- [ ] Proper cleanup in onCleared
- [ ] Configuration changes handled

### Security ✅
- [ ] EncryptedSharedPreferences for sensitive data
- [ ] No hardcoded API keys/secrets
- [ ] No sensitive data in logs
- [ ] HTTPS only
- [ ] Input validation

### Performance ✅
- [ ] Network/DB on background threads
- [ ] No memory leaks
- [ ] DiffUtil in RecyclerView
- [ ] Image caching (Coil/Glide)
- [ ] Database queries optimized

This comprehensive standards document provides detailed guidance for Android code reviews in the Payoo Android application.
