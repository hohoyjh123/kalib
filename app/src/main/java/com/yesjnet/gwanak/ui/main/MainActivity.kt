package com.yesjnet.gwanak.ui.main

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.View
import android.webkit.GeolocationPermissions
import android.webkit.JsResult
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.R
import com.yesjnet.gwanak.core.ConstsApp
import com.yesjnet.gwanak.core.ConstsData
import com.yesjnet.gwanak.core.EnumApp
import com.yesjnet.gwanak.core.GAApplication
import com.yesjnet.gwanak.data.model.MemberInfo
import com.yesjnet.gwanak.data.model.PushData
import com.yesjnet.gwanak.data.model.eventbus.EBMainPageEvent
import com.yesjnet.gwanak.data.model.eventbus.EBMemberInfo
import com.yesjnet.gwanak.databinding.ActivityMainBinding
import com.yesjnet.gwanak.extension.OnSingleClickListener
import com.yesjnet.gwanak.extension.browse
import com.yesjnet.gwanak.extension.showAlertConfirm
import com.yesjnet.gwanak.extension.showAlertOK
import com.yesjnet.gwanak.fcm.LocalNotificationManager
import com.yesjnet.gwanak.storage.SecurePreference
import com.yesjnet.gwanak.ui.NavScreen
import com.yesjnet.gwanak.ui.ScreenInfo
import com.yesjnet.gwanak.ui.base.BaseAppBarActivity
import com.yesjnet.gwanak.ui.base.BaseDialogFragment
import com.yesjnet.gwanak.ui.startScreen
import com.yesjnet.gwanak.util.PermissionUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.math.sqrt
import androidx.core.net.toUri


class MainActivity: BaseAppBarActivity<ActivityMainBinding>(R.layout.activity_main),
    SensorEventListener, OnSingleClickListener {

    private val pref: SecurePreference by inject()
    private val localNotifyManager: LocalNotificationManager by inject()
    private lateinit var sensorManager: SensorManager
    private var accel: Float = 0f
    private var accelCurrent: Float = 0f
    private var accelLast: Float = 0f
    private var showQrcodeDialog = false // QR코드 팝업 활성 여부 (흔들어 열기에서 한개만 띄워야함)

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onInitView() {
        binding.viewModel = getViewModel()
        binding.lifecycleOwner = this
        binding.activity = this
        EventBus.getDefault().register(this)

        Handler(Looper.getMainLooper()).postDelayed({
            Logger.d("[FCM] onCreate intent - $intent")
            fcmMessageLanding(intent)
        }, 500)

        val memberInfo = binding.viewModel?.userInfo?.getMember()
//        binding.icMainBottom.mainTab1.isActivated = true

        // SensorManager 초기화
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        accel = 10f
        accelCurrent = SensorManager.GRAVITY_EARTH
        accelLast = SensorManager.GRAVITY_EARTH

        doNotification()
        setSwipeToRefresh()
        initWebView()
        setWebView(EnumApp.WebType.HOME, memberInfo)

        // autu login check
//        binding.viewModel?.checkAutoLogin()
        // todo 서버 응답없어서 임시 클릭 이벤트... 서버 정상되면 삭제 예정
        // 테스트 후 주석 start
//        binding.btLogin.setOnClickListener { startScreen(NavScreen.Login(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))) }
//        binding.btSetting.setOnClickListener { startScreen(NavScreen.Setting(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE))) }
        // 테스트 후 주석 end
    }

    override fun onSubscribeUI() {
        binding.viewModel?.apply {
            onErrorResource.observe(this@MainActivity) {
                showAlertOK(message = it.message)
            }
            onNavScreen.observe(this@MainActivity) {
                startScreen(it)
            }
            onDataLoading.observe(this@MainActivity) {
                if (!it)
                    binding.srlRefresh.isRefreshing = false
            }
            onSelectTab.observe(this@MainActivity) {
                when (it) {
                    EnumApp.MainPage.HOME -> {
//                            binding.icMainBottom.mainTab1.isActivated = true
                        setWebView(EnumApp.WebType.HOME, binding.viewModel?.userInfo?.getMember())
                    }
                    EnumApp.MainPage.BOOK_SEARCH -> {
//                            binding.icMainBottom.mainTab2.isActivated = true
                        setWebView(EnumApp.WebType.BOOK_SEARCH, binding.viewModel?.userInfo?.getMember())
                    }
                    EnumApp.MainPage.MY_LIBRARY -> {
//                            binding.icMainBottom.mainTab2.isActivated = true
                        setWebView(EnumApp.WebType.MY_LIBRARY, binding.viewModel?.userInfo?.getMember())
                    }
                    EnumApp.MainPage.SETTING -> {
//                            binding.icMainBottom.mainTab4.isActivated = true
                        startScreen(NavScreen.Setting(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE)))

                    }
                    EnumApp.MainPage.MENU -> {
//                            binding.icMainBottom.mainTab5.isActivated = true
                        startScreen(NavScreen.AllMenu(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE)))
                    }
                    else -> EnumApp.WebType.HOME
                }
            }

            // 자동 로그인 실패 (웹페이지에서 비밀번호 변경시)
            onAutoLoginError.observe(this@MainActivity) {
//                showAlertOK(message = it, okListener = object : BaseDialogFragment.MyOnClickListener {
//                    override fun onClick(obj: Any?) {
//                        startScreen(NavScreen.Membership(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE)))
//                    }
//                } )
            }

            // 회원정보
            onMemberInfo.observe(this@MainActivity) {
                Logger.d("mainactivity memberinfo")
                // 자동로그인 개인쟁보재동의 N 인 경우 재동의 페이지 이동
                if (it.userId.isNotEmpty() && !EnumApp.FlagYN.booleanByStatus(it.reAgreeYn)) {
                    setWebView(EnumApp.WebType.RE_AGREE_PERSONAL_INFOMATION, it)
                }
            }
        }
    }

    override fun onBackPressed() {
//        if (!checkBack()) {
            exitDialog()
//        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // 가속도 계산
        accelLast = accelCurrent
        accelCurrent = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta = accelCurrent - accelLast
        accel = accel * 0.9f + delta // 저항을 적용한 가속도 계산

        // 흔들림 감지
        if (accel > 12) { // 특정 값을 넘으면 흔들림으로 간주
            val isShake = pref.getConfigBool(ConstsData.PrefCode.SHAKE_FLAG, false)
            if (isShake) {
                val userId = binding.viewModel?.onMemberInfo?.value?.userId ?: ""
                if (userId.isNullOrEmpty()) {
                    showAlertOK(message = getString(R.string.qrcode_associate_member_error_msg))
                } else {
                    startScreen(NavScreen.Login(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE)))
                }
            }  // 흔들림 시 QR코드 팝업
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 정확도 변화 감지 시 처리 (필요시 구현)
    }

    private fun fcmMessageLanding(intent: Intent) {
        val pushData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(ConstsApp.IntentCode.PUSHDATA, PushData::class.java)
        } else {
            intent.getSerializableExtra(ConstsApp.IntentCode.PUSHDATA) as? PushData
        }
        Logger.d("[FCM] fcmMessageLanding intent.PushData = $pushData")
        pushData?.let {
            localNotifyManager.cancelAllNotification()
            onEventSelectPage(it)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventSelectPage(pushData: PushData) {
        Logger.d("onEventSelectPage pushData = $pushData")
        startScreen(pushData)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMain(event: EBMainPageEvent) {
        Logger.d("event = ${event.page.name}")
        binding.viewModel?.setSelectTab(event.page)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMemberInfo(event: EBMemberInfo) {
        if (event.memberInfo.userId.isNullOrEmpty()) {
            binding.viewModel?.updateMemberInfo(MemberInfo())
            setWebView(EnumApp.WebType.HOME, MemberInfo())
        } else {
            binding.viewModel?.updateMemberInfo(event.memberInfo)
            setWebView(EnumApp.WebType.HOME, event.memberInfo)
        }
    }

    fun startScreen(pushData: PushData) {
        val url = pushData.url
        if (url.isNullOrEmpty()) {
            Logger.d("jihoon push url null")
        } else {
            val memberInfo = binding.viewModel?.userInfo?.getMember()
            val fullUrl = "${ConstsData.SERVER_URL_FULL}mobile/api/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId ?: "", "utf-8")}&returnUrl=${URLEncoder.encode(url, "utf-8")}"
            binding.wv.loadUrl(fullUrl)
        }
    }

    /**
     * 앱 종료 팝업
     */
    private fun exitDialog() {
        showAlertConfirm(title = getString(R.string.alarm), message = getString(R.string.activity_finish_message), okListener = object: BaseDialogFragment.MyOnClickListener {
            override fun onClick(obj: Any?) {
                finish()
            }
        })
    }

    private fun doNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionUtil.requestNotifications {
//                CoroutineScope(Dispatchers.Main).launch {
//                    delay(1000)
//                    val pushData =
//                        PushData("푸시 알림 제목", "푸시 알림 내용 테스트입니다.")
//
//                    localNotifyManager.showLocalNotification(pushData)
//                }
            }
        }
    }

    /**
     * 하단메뉴 버튼 클릭 처리
     */
    private fun selectPage(selectPage: EnumApp.MainPage) {
        // todo
        Logger.d("selectPage = ${selectPage.name}")
    }

    // 다이얼로그가 닫혔을 때 플래그를 리셋하는 함수
    fun onDialogDismissed() {
        Logger.d("jihoon onDialogDismissed")
        showQrcodeDialog = false
    }

    // webview start
    private fun setWebView(webType: EnumApp.WebType, memberInfo: MemberInfo? = null) {
        val url = "${ConstsData.SERVER_URL_FULL}mobile/api/appReLogin.do?userId=${URLEncoder.encode(memberInfo?.userId ?: "", "utf-8")}&returnUrl=${URLEncoder.encode(webType.webViewUrl, "utf-8")}"
        binding.wv.loadUrl(url)
    }

    /**
     * 웹뷰 이전 페이지 존재 여부
     * return bool (true - 존재, false - 미존재라 앱 종료)
     */
    private fun checkBack(): Boolean {
        return if (binding.wv.canGoBack()) {
            binding.wv.goBack()
            true
        } else {
            false
        }
    }

    /**
     * 당겨서 새로 고침
     */
    private fun setSwipeToRefresh() {
        binding.srlRefresh.setOnRefreshListener {
//            val selectTab = binding.viewModel?.onSelectTab?.value ?: EnumApp.MainPage.HOME
//            val webType = when (selectTab) {
//                EnumApp.MainPage.HOME -> {
//                    EnumApp.WebType.HOME
//                }
//                EnumApp.MainPage.BOOK_SEARCH -> {
//                    EnumApp.WebType.BOOK_SEARCH
//                }
//                EnumApp.MainPage.LOAN_STATUS -> {
//                    EnumApp.WebType.LOAN_STATUS
//                }
//                EnumApp.MainPage.BOOK_INTEREST -> {
//                    EnumApp.WebType.BOOK_INTEREST
//                }
//                else -> EnumApp.WebType.HOME
//            }
            binding.wv.reload()
        }

        // 웹뷰 SwipeRefreshLayout 예외처리
        binding.wv.getViewTreeObserver().addOnScrollChangedListener {
            if (binding.wv.scrollY === 0) {
                binding.srlRefresh.setEnabled(true)
            } else {
                binding.srlRefresh.setEnabled(false)
            }
        }
    }

    private fun initWebView() {
        binding.wv.settings.javaScriptEnabled = true
        binding.wv.settings.setGeolocationEnabled(true)
        binding.wv.settings.userAgentString = binding.wv.settings.userAgentString
        binding.wv.settings.domStorageEnabled = true
        binding.wv.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_INSET
        binding.wv.settings.allowFileAccess = true
        binding.wv.settings.textZoom = 100
        binding.wv.webChromeClient = MyWebChromeClient()
        binding.wv.webViewClient = WebViewClient()

        binding.wv.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            Logger.d("jihoon setDownloadListener")
            // 파일명 잘라내기 및 확장자 확인
            val _contentDisposition = URLDecoder.decode(contentDisposition,"UTF-8")
            var fileName = _contentDisposition
            if (fileName != null && fileName.isNotEmpty()) {
                val idxFileName = fileName.indexOf("filename=")
                if (idxFileName > -1) {
                    fileName = fileName.substring(idxFileName + 9).trim { it <= ' ' }
                }
                if (fileName.endsWith(";")) {
                    fileName = fileName.substring(0, fileName.length - 1)
                }
                if (fileName.startsWith("\"") && fileName.startsWith("\"")) {
                    fileName = fileName.substring(1, fileName.length - 1)
                }
            } else {
                // 파일명(확장자포함) 확인이 안되었을 때 기존방식으로 진행
                fileName = URLUtil.guessFileName(url, _contentDisposition, mimetype)
            }

            val request = DownloadManager.Request(Uri.parse(url))

            request.setMimeType(mimetype)
            val cookies = android.webkit.CookieManager.getInstance().getCookie(url)
            request.addRequestHeader("cookie", cookies)
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Downloading file...")
            request.setTitle(fileName)
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
        }
    }


    inner class MyWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
//    		super.onProgressChanged(view, newProgress);
//    		bar.setProgress( (int)(20+ (float)newProgress / 100 * 80));
            if (newProgress >= 100) {
                binding.viewModel?.onDataProgress?.value = false
            }
            Logger.d("WebView progress=$newProgress url=${view.url}")
        }

        // 웹뷰 위치권한
        override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback)
//            PermissionUtil.requestLocation {
            callback.invoke(origin, true, true)
//            }
        }

        override fun onJsAlert(
            view: WebView,
            url: String,
            message: String,
            result: JsResult,
        ): Boolean {
            Logger.d("onJsAlert url=$url message=$message")
            binding.viewModel?.onDataProgress?.value = false

            showAlertOK(
                title = getString(R.string.alarm),
                message = message,
                okListener = object : BaseDialogFragment.MyOnClickListener {
                    override fun onClick(obj: Any?) {
                        view.post { // <- 여기 포인트
                            result.confirm()
                        }
                    }
                }
            )
            return true
        }
    }


    inner class WebViewClient : android.webkit.WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Logger.d("onPageStarted")
            binding.viewModel?.onDataProgress?.value = true
        }

        //        override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
//            url?.let { view.loadUrl(it) }
//            return true
//        }
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            Logger.d("WEBVIEW = $url")
            if (url.indexOf("twitter.com/share") >= 0 || url.indexOf("www.facebook.com/share") >= 0 || url.indexOf(
                    "me2day.net/posts"
                ) >= 0 || url.indexOf("kakaolink://sendurl") >= 0 || url.indexOf("yes24lib-phone") >= 0 || url.indexOf(
                    "yes24lib-pad"
                ) >= 0 || url.indexOf("Kyobolibraryt3") >= 0 || url.indexOf("bookcubeshelf:") >= 0 || url.indexOf(
                    "bookPlayer:"
                ) >= 0 || url.indexOf("bookPlayerhd:") >= 0 || url.indexOf("market://") >= 0
            ) {
                var canOpen = true
                try {
                    val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(myIntent)
                } catch (e: Exception) {
                    canOpen = false
                }
                if (canOpen) view.loadUrl("javascript:clearTimeout(AppCheckTimer);")
                return true
            } else if (url.indexOf("kr.co.jnet.gwanak") >= 0) {
                return try {
                    if (url.contains("openUrl")) {
                        val param: Map<*, *> = parseParam(url)
                        val urlTo = param["url"] as String?
                        val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse(urlTo))
                        startActivity(myIntent)
                        true
                    } else {
                        loadUrlScheme(url)
                        true
                    }
                } catch (e: Exception) {
                    true
                }
            // url 마지막에 포함되면 외부 브라우저로 이동처리
            } else if (url.endsWith("contents.do")) {
                browse(url)
                true
            }
            else if (url.endsWith("index.do?")) {
                binding.viewModel?.setSelectTab(EnumApp.MainPage.HOME)
              true
            }
            else if (url.indexOf("rtsp://") >= 0 || url.indexOf(".mp4") >= 0) {
                val prefs = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.isConnectedOrConnecting
                if (prefs.getBoolean("KEY_3G", true) || isWifi) {
//                    val intent = Intent(this@MainActivity, VideoViewActivity::class.java)
//                    intent.putExtra("url", url)
//                    startActivity(intent)
                } else {
                    showAlertConfirm(title = "알림", message = "3 ", okListener = object : BaseDialogFragment.MyOnClickListener {
                        override fun onClick(obj: Any?) {
                            Logger.d("알림 3 ok")
//                            val intent = Intent(requireContext(), VideoViewActivity::class.java)
//                            intent.putExtra("url", url)
//                            startActivity(intent)
                        }

                    })
                }
            } else if (url.startsWith("intent://")) {
                try {
                    val intent = Intent.parseUri(url, 0)
                    var packageAvailable = false
                    val list: List<ResolveInfo> = packageManager
                        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                    packageAvailable = if (list == null) false else list.size > 0
                    if (packageAvailable) {
                        startActivity(intent)
                    } else {
                        if (intent != null && url.startsWith("intent:")) {
                            val appPackageName = intent.getPackage()
                            try {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW, Uri.parse(
                                            "market://details?id=$appPackageName"
                                        )
                                    )
                                )
                            } catch (anfe: ActivityNotFoundException) {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW, Uri.parse(
                                            "http://play.google.com/store/apps/details?id=$appPackageName"
                                        )
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return true
            // 다이얼
            } else if (url.startsWith("tel:")) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = url.toUri()
                }
                view.context?.startActivity(intent)
                return true
            // 메일
            } else if (url.startsWith("mailto:")) {
                val mailUri = url.toUri()
                val email = mailUri.schemeSpecificPart // test@naver.com 부분 추출
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:$email".toUri()
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                }
                try {
                    view.context?.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return true
            } else {
                if ( url == "https://lib.gwanak.go.kr/galib/program/parcelMemberApply.do") {
                    return false
                } else {
                    view.loadUrl(url)
                }
            }
            return false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Logger.d("onPageFinished")
//            binding.viewModel?.onDataLoading?.value = false
            binding.viewModel?.onDataProgress?.value = false
        }
    }

    @Throws(UnsupportedEncodingException::class)
    fun parseParam(url: String): Map<String, String> {
        val mParams = mutableMapOf<String, String>()
        val params = url.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        if (params.size > 1) {
            val pv = params[1].split("&".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            for (i in pv.indices) {
                val ss = pv[i].split("=".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                mParams[ss[0]] = URLDecoder.decode(ss[1], "utf-8")
            }
        }
        return mParams
    }

    /**
     * webview 스크립트 이동처리
     */
    fun loadUrlScheme(url: String) {
//        if (url.startsWith(EnumApp.WebScheme.OPEN_LOGIN.scheme)) {
//            startScreen(NavScreen.Login(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE)))
//        } else if (url.startsWith(EnumApp.WebScheme.OPEN_BARCODE.scheme)) {
//            val userNo = binding.viewModel?.onMemberInfo?.value?.userNo ?: ""
//            if (userNo.isNullOrEmpty()) {
//                showAlertOK(message = getString(R.string.qrcode_associate_member_error_msg))
//            } else {
//                Logger.d("qrcode")
//            }
//        } else if (url.startsWith(EnumApp.WebScheme.APP_SETTING.scheme)) {
//            startScreen(NavScreen.Setting(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE)))
//        } else {
//            Logger.d("loadUrl ")
//        }
        // 서버에서 https:// 부터 내려주기에 아래로 수정했는데 서버에서 https:// 안주고 kr 부터 주면 아래 코드는 제거, 위 주석 해제
        if (url.indexOf(EnumApp.WebScheme.OPEN_LOGIN.scheme) >= 0) {
            startScreen(NavScreen.Login(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE)))
        } else if (url.indexOf(EnumApp.WebScheme.OPEN_BARCODE.scheme) >= 0) {
            val userId = binding.viewModel?.onMemberInfo?.value?.userId ?: ""
            if (userId.isNullOrEmpty()) {
                showAlertOK(message = getString(R.string.qrcode_associate_member_error_msg))
            } else {
                startScreen(NavScreen.Login(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE)))
            }
        } else if (url.indexOf(EnumApp.WebScheme.APP_SETTING.scheme) >= 0) {
            startScreen(NavScreen.Setting(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE)))
        } else {
            Logger.d("loadUrl ")
        }
    }

    override fun onSingleClick(view: View) {
        when (view.id) {
            R.id.ivBanner -> {
                val memberInfo = binding.viewModel?.onMemberInfo?.value
                setWebView(EnumApp.WebType.HOME, memberInfo)
            }
            R.id.ivBarcode -> {
                val memberInfo = binding.viewModel?.onMemberInfo?.value
                if (memberInfo == null || memberInfo.userId.isNullOrEmpty()) {
                    loginErrorDialog()
                } else {
                    val memberClass = EnumApp.MemberClass.valueOfType(memberInfo.userClass)
                    if (EnumApp.MemberClass.FULL_MEMBER == memberClass) {
                        startScreen(NavScreen.Login(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE)))
                    } else {
                        showAlertOK(message = getString(R.string.associate_member_error))
                    }
                }
            }
        }
    }
    // webview end

    private fun loginErrorDialog() {
        showAlertOK(message = getString(R.string.available_after_logging_in), okListener = object : BaseDialogFragment.MyOnClickListener {
            override fun onClick(obj: Any?) {
                startScreen(NavScreen.Login(screenInfo = ScreenInfo(transType = EnumApp.TransitionType.SLIDE)))
            }
        })
    }
}