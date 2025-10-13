package com.yesjnet.gwanak.ui.base

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.ViewDataBinding
import com.yesjnet.gwanak.R
import com.yesjnet.gwanak.core.ConstsApp
import com.yesjnet.gwanak.core.EnumApp
import com.yesjnet.gwanak.extension.gone
import com.yesjnet.gwanak.extension.setOnSingleClickListener
import com.yesjnet.gwanak.extension.show
import com.yesjnet.gwanak.extension.slideTransitionFinish
import com.yesjnet.gwanak.extension.slideTransitionUpFinish

/**
 * base activity(앱바 사용)
 */
abstract class BaseAppBarActivity<T : ViewDataBinding>(@LayoutRes private val layoutResId: Int) :
    BaseActivity<T>(layoutResId) {

    lateinit var appbarStyle: EnumApp.AppBarStyle
    var transitionType: EnumApp.TransitionType? = null
    private var backClick: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transitionType =
            intent.getSerializableExtra(ConstsApp.IntentCode.UI_TRANSITION_TYPE) as? EnumApp.TransitionType
    }

    /**
     *  AppBar에 Home icon 공통 이벤트 처리(뒤로가기 버튼)
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> if (backClick == null) finish() else backClick?.invoke()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        when (transitionType) {
            EnumApp.TransitionType.SLIDE -> slideTransitionFinish()
            EnumApp.TransitionType.UP -> slideTransitionUpFinish()
            else -> {
            }
        }
    }

    /**
     * 앱바 스타일 설정
     * @param appBarStyle 헤더 스타일 (back - 뒤로가기, close - 팝업)
     * @param title 화면 타이틀
     * @param titleImg 화면 타이틀 이미지(현재는 커뮤니티 게시글 상세 에서만 쓰임)
//     * @param mainPage 메인타입
     * @param rightText 화면 오른쪽 텍스트
     * @param thirdClick 세번째클릭
     * @param leftClick 두번째 클릭 (일반적으로 버튼 2개인경우 왼쪽)
     * @param rightClick 첫번째 클릭 (버튼 2개이상인 경우 맨 오른쪽)
     * Next... 고정뷰 Visble변경 말고 화면별 커스텀 컴포넌트(View,Drawable) AddView로 개선 with setSupportActionBar(*)
     */
    fun initHeader(
        appBarStyle: EnumApp.AppBarStyle = EnumApp.AppBarStyle.NONE,
        title: String? = null,
        titleImg: Int? = null,
//        mainPage: EnumApp.MainPage? = null,
        rightText: String? = null,
        backClick: (() -> Unit)? = null,
        thirdClick: (() -> Unit)? = null,
        leftClick: (() -> Unit)? = null,
        rightClick: (() -> Unit)? = null
    ) {
        appbarStyle = appBarStyle
        var toolbar: Toolbar? = findViewById(R.id.toolbar)
        if (toolbar != null) {
            val clMain: ConstraintLayout = findViewById(R.id.clMain)
            val clBack: ConstraintLayout = findViewById(R.id.clBack)
            val ivBack: ImageView = findViewById(R.id.ivBack)
            val tvTitle: AppCompatTextView = findViewById(R.id.tvToolbarTitle)
            val btnClose: ImageView = findViewById(R.id.ibtClose)
            val tvRightText: AppCompatTextView = findViewById(R.id.tvRightText)
            val ibtLeft: AppCompatImageButton = findViewById(R.id.ibtLeft)
            val ibtRight: AppCompatImageButton = findViewById(R.id.ibtRight)

            // View
            when (appbarStyle) {
                EnumApp.AppBarStyle.NONE -> {
                    clBack.gone()
                    btnClose.gone()
                    ibtLeft.gone()
                    ibtRight.gone()
                    tvTitle.text = ""
                }

                EnumApp.AppBarStyle.TITLE -> {
                    tvTitle.text = title
                    tvTitle.show()
                }

                EnumApp.AppBarStyle.BACK -> {
                    clBack.show()
                    ivBack.setImageResource(appBarStyle.leftView.defaultResId)
                }

                EnumApp.AppBarStyle.CLOSE -> {
                    btnClose.show()
                }

                EnumApp.AppBarStyle.BACK_TITLE -> {
                    clBack.show()
                    ivBack.setImageResource(appBarStyle.leftView.defaultResId)
                    tvTitle.text = title
                    tvTitle.show()
                }

                EnumApp.AppBarStyle.TITLE_CLOSE -> {
                    tvTitle.text = title
                    tvTitle.show()
                    btnClose.show()
                }

                EnumApp.AppBarStyle.TITLE_TEXT -> {
                    tvTitle.text = title
                    tvTitle.show()
                    tvRightText.text = rightText
                    tvRightText.show()
                }

                EnumApp.AppBarStyle.BACK_TITLE_TEXT -> {
                    clBack.show()
                    ivBack.setImageResource(appBarStyle.leftView.defaultResId)
                    tvTitle.text = title
                    tvTitle.show()
                    tvRightText.text = rightText
                    tvRightText.show()
                }
                EnumApp.AppBarStyle.BACK_TITLE_CLOSE -> {
                    clBack.show()
                    ivBack.setImageResource(appBarStyle.leftView.defaultResId)
                    tvTitle.text = title
                    tvTitle.show()
                    btnClose.show()
                }
            }

            // Event
            when (appBarStyle) {
                // close 액션
                EnumApp.AppBarStyle.CLOSE,
                EnumApp.AppBarStyle.TITLE_CLOSE -> {
                    btnClose.setOnSingleClickListener {
                        rightClick?.invoke() ?: finish()
                    }
                }
                EnumApp.AppBarStyle.BACK_TITLE_CLOSE -> {
                    ibtLeft.setOnSingleClickListener {
                        backClick?.invoke() ?: finish()
                    }
                    btnClose.setOnSingleClickListener {
                        rightClick?.invoke() ?: finish()
                    }
                }

                // 그 외 액션
                else -> {
                    clBack.setOnSingleClickListener {
                        backClick?.invoke() ?: finish()
                    }
                    ibtLeft.setOnSingleClickListener {
                        leftClick?.let { it() }
                    }
                    tvRightText.setOnSingleClickListener {
                        rightClick?.let { it() }
                    }
                    ibtRight.setOnSingleClickListener {
                        rightClick?.let { it() }
                    }
                }
            }

            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        } else {
            throw NoSuchElementException("Your Activity has no Toolbar !!! ")
        }
    }

    fun setLeftBtnVisible(isVisible: Boolean) {
        val ibtLeft: AppCompatImageButton = findViewById(R.id.ibtLeft)
        if (isVisible) {
            ibtLeft.show()
        } else {
            ibtLeft.gone()
        }
    }

    /**
     * 타이틀 업데이트
     *
     * @param title 타이틀 문구
     */
    fun setTitle(title: String) {
        val tvTitle: AppCompatTextView = findViewById(R.id.tvToolbarTitle)
        tvTitle.text = title
        tvTitle.show()
    }

}