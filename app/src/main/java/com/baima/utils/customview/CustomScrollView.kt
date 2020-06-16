package com.baima.utils.customview

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.EdgeEffect
import android.widget.OverScroller
import android.widget.Scroller
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import com.baima.utils.utils.dp2px
import com.baima.utils.utils.getScreenHeight
import java.lang.Integer.max
import java.util.jar.Attributes
import kotlin.math.abs
import kotlin.math.min

class CustomScrollView : ViewGroup {

    constructor(context: Context):this(context, null)
    constructor(context: Context,attributes: AttributeSet?):this(context,attributes,0)
    constructor(context: Context,attributes: AttributeSet?,defStyleAttr:Int):super(context,attributes,defStyleAttr)

    private var mScroller :OverScroller
    private var mEdgeEffectTop :EdgeEffect
    private var mEdgeEffectBottom :EdgeEffect

    private var dp = 0
    private var childHeight = 0
    private var mScreenHeight = 0
    private var mViewHeight = 0

    private val mOverScrollDistance = 10000

    private var mIsBeingDragged = false
    private var mLastY = 0f
    private var mSecondLastY = 0f
    private var secondPointerId = INVALID_ID
    private var activePointerId = INVALID_ID

    private var mMinFlingVelocity = 0
    private var mMaxFlingVelocity = 0
    private var mVelocityTracker:VelocityTracker? = null
    private var mTouchSlop = 0 //当手指移动大于这个常量，便表示手指开始拖动

    init {
        val viewConfiguration = ViewConfiguration.get(context)
        mMinFlingVelocity = viewConfiguration.scaledMinimumFlingVelocity
        mMaxFlingVelocity = viewConfiguration.scaledMaximumFlingVelocity
        mTouchSlop = viewConfiguration.scaledTouchSlop //当手指移动大于这个常量，便表示手指开始拖动
        mScreenHeight = getScreenHeight(context as Activity)
        dp = dp2px(1f,context)
        childHeight = 200*dp
        mScroller = OverScroller(context)
        mEdgeEffectBottom = EdgeEffect(context)
        mEdgeEffectTop = EdgeEffect(context)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

        for(i in 0 until childCount){
            val child = getChildAt(i)
            if(child.visibility!=View.GONE){
                child.layout(0,i* childHeight,r,(i+1)* childHeight)
            }
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val count = childCount
        for(i in 0 until count){
            val child = getChildAt(i)
            measureChild(child,widthMeasureSpec,heightMeasureSpec)
        }
        mViewHeight = childCount*childHeight

        setMeasuredDimension(measureSize(widthMeasureSpec, dp2px(400f,context)),measureSize(heightMeasureSpec,
            mViewHeight))
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(!mEdgeEffectTop.isFinished){
            val restoreCount = canvas!!.save()
            val width = width - paddingLeft - paddingRight
            canvas.translate(paddingLeft.toFloat(),min(0,scrollY).toFloat())
            mEdgeEffectTop.setSize(width,height)
            if(mEdgeEffectTop.draw(canvas)){
                postInvalidateOnAnimation()
            }
            canvas.restoreToCount(restoreCount)
        }

        if(!mEdgeEffectBottom.isFinished){
            val restoreCount = canvas!!.save()
            val width = width - paddingLeft - paddingRight
            canvas.translate(-width+paddingLeft.toFloat(),max(getScrollRange(),scrollY+height).toFloat())
            canvas.rotate(180f,width.toFloat(),0f)
            mEdgeEffectBottom.setSize(width,height)
            if(mEdgeEffectBottom.draw(canvas)){
                postInvalidateOnAnimation()
            }
            canvas.restoreToCount(restoreCount)
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {

            if(mVelocityTracker==null){
                mVelocityTracker = VelocityTracker.obtain()
            }
            mVelocityTracker!!.addMovement(event)
            when(event.actionMasked){
                MotionEvent.ACTION_DOWN ->{
                    val activePointerIndex = event.actionIndex
                    activePointerId = event.findPointerIndex(activePointerIndex)
                    mLastY = event.y

                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }

                    if(!mScroller.isFinished){mScroller.abortAnimation()}

                    mIsBeingDragged = false
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    val activePointerIndex = event.actionIndex
                    secondPointerId = event.findPointerIndex(activePointerIndex)
                    mSecondLastY = event.getY(activePointerIndex)
                }

                MotionEvent.ACTION_MOVE ->{
                    if(!mScroller.isFinished){
                        mScroller.abortAnimation()
                    }

                    if(secondPointerId!= INVALID_ID){
                        val index = event.findPointerIndex(secondPointerId)
                        mSecondLastY = event.getY(index)
                    }

                    var dy = mLastY - event.y

                    if(!mIsBeingDragged&&abs(dy)>mTouchSlop){
                        mIsBeingDragged = true

                        if(dy>0){
                            dy-=mTouchSlop
                        }else{
                            dy+=mTouchSlop
                        }
                    }
                    //手指拖动 Drag
                    if(mIsBeingDragged){
                        overScrollBy(0,(dy+0.5f).toInt(),0,scrollY,0,getScrollRange(),0,getScrollRange()+mOverScrollDistance,true)
                        val pulledToy = scrollY + dy
                        mLastY = event.y
                        if(pulledToy<0){
                            mEdgeEffectTop.onPull(dy/height,event.getX(activePointerId)/width)
                            if(!mEdgeEffectBottom.isFinished){
                                mEdgeEffectBottom.onRelease()
                            }
                        }else if(pulledToy>getScrollRange()){
                            mEdgeEffectBottom.onPull(dy/height,1f-event.getX(activePointerId)/width)
                            if(!mEdgeEffectTop.isFinished){
                                mEdgeEffectTop.onRelease()
                            }
                        }

                        if(mEdgeEffectTop.isFinished && mEdgeEffectBottom.isFinished){
                            postInvalidate()
                        }
                    }
                    0
                }

                MotionEvent.ACTION_POINTER_UP ->{
                    val curIndex = event.actionIndex
                    val curId = event.findPointerIndex(curIndex)
                    if(curId == activePointerId){
                        mLastY = mSecondLastY
                        activePointerId = secondPointerId
                        secondPointerId = INVALID_ID
                        mSecondLastY = 0f
                    }else{
                        secondPointerId = INVALID_ID
                        mSecondLastY = 0f
                    }
                }

                MotionEvent.ACTION_UP ->{

                    if(mIsBeingDragged){
                        mVelocityTracker!!.computeCurrentVelocity(1000,mMaxFlingVelocity.toFloat())
                        val velocity = mVelocityTracker!!.getYVelocity(activePointerId)
                        mVelocityTracker!!.computeCurrentVelocity(1000)
                        if(abs(velocity)>mMinFlingVelocity){
                            doFling(-velocity)
                        }
                    }
                    mIsBeingDragged = false
                    mLastY = 0f
                    activePointerId = INVALID_ID
                    mVelocityTracker!!.clear()
                }

                else -> {

                }
            }
        }

        return true
    }

    private fun getScrollRange(): Int {
        return mViewHeight - mScreenHeight+200
    }


    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        if(!mScroller.isFinished){
            val oldX = scrollX
            val oldY = scrollY
            scrollTo(scrollX,scrollY)
            onScrollChanged(scrollX,scrollY,oldX,oldY)
            if(clampedY){
                 mScroller.springBack(scrollX,scrollY,0,0,0,getScrollRange())
            }
        }else{
            super.scrollTo(scrollX,scrollY)
        }
    }

    private fun doFling(fl: Float) {
        mScroller.fling(0,scrollY,0,(fl+0.5f).toInt(),0,0,-5000,10000)
        invalidate()
    }

    override fun computeScroll() {
        super.computeScroll()
        if(mScroller.computeScrollOffset()){
            val oldX = scrollX
            val oldY = scrollY
            val x = mScroller.currX
            val y = mScroller.currY

            val range = getScrollRange()
            if (oldX != x || oldY != y) {
                overScrollBy(x-oldX,y-oldY,oldX,oldY,0,range,0,range+mOverScrollDistance,false)
            }
            val overScrollMode = overScrollMode
            val canOverScroll = overScrollMode == OVER_SCROLL_ALWAYS ||
                    (overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0)
            if (canOverScroll) {
                if (y<0 && oldY >= 0) {
                    mEdgeEffectTop.onAbsorb(mScroller.currVelocity.toInt())
                } else if (range in (oldY + 1) until y) {
                    mEdgeEffectBottom.onAbsorb(mScroller.currVelocity.toInt())
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mVelocityTracker!!.recycle()
        mVelocityTracker = null
    }




    private fun measureSize(measureSpec:Int,default:Int):Int{
        val size = MeasureSpec.getSize(measureSpec)
        val mode = MeasureSpec.getMode(measureSpec)
        if(mode == MeasureSpec.EXACTLY){
            return size
        }else{
            if (mode == MeasureSpec.AT_MOST){
                return min(default,size)
            }
            return default
        }
    }

    companion object{
        const val INVALID_ID = -5
    }

}