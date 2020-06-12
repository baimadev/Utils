package com.baima.utils.utils

import android.content.Context

class WorkSingleton private constructor(context:Context) {
    init {

    }

    companion object: SingletonHolder<WorkSingleton, Context>(::WorkSingleton) //传入构造函数 (creator:(A)->T)

}

open class SingletonHolder<out T,in A>(creator:(A)->T){
    private var creator:((A)->T)?= creator
    @Volatile
    private var instance :T? = null

    fun getInstance(arg:A):T{
        val i = instance
        if(i!=null){
            return  i
        }
        return synchronized(this){
            val i2 = instance
            if(i2!=null){
                i2
            }else{
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}