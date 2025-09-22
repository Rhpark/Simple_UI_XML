package kr.open.library.simple_ui.logcat.extensions

import kr.open.library.simple_ui.logcat.Logx


/**
 * 모든 객체에 대한 로깅 확장 함수
 * 메서드 체이닝으로 간편한 사용 가능
 */

public /*inline*/ fun Any.logxD(): Unit = Logx.d1(this)
public /*inline*/ fun Any.logxD(tag: String): Unit = Logx.d1(tag, this)

public /*inline*/ fun Any.logxV(): Unit = Logx.v1(this)
public /*inline*/ fun Any.logxV(tag: String): Unit = Logx.v1(tag, this)

public /*inline*/ fun Any.logxW(): Unit = Logx.w1(this)
public /*inline*/ fun Any.logxW(tag: String): Unit =  Logx.w1(tag,this)

public /*inline*/ fun Any.logxI(): Unit = Logx.i1(this)
public /*inline*/ fun Any.logxI(tag: String): Unit = Logx.i1(tag,this)

public /*inline*/ fun Any.logxE(): Unit = Logx.e1(this)
public /*inline*/ fun Any.logxE(tag: String): Unit = Logx.e1(tag,this)

public /*inline*/ fun String.logxJ(): Unit = Logx.j1(this)
public /*inline*/ fun String.logxJ(tag: String): Unit = Logx.j1(tag,this)

public /*inline*/ fun Any.logxP(): Unit = Logx.p1(this)
public /*inline*/ fun Any.logxP(tag:String): Unit = Logx.p1(tag,this)
