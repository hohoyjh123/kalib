package com.yesjnet.gwanak.data.net

/**
 * api result
 */
interface APIResult<V> {
    /**
     * indicate the resource loading
     *
     * @param isLoading true if loading false otherwise
     */
    fun onLoading(isLoading: Boolean)

    /**
     * @param t errorResource
     */
    fun onError(errorResource: ErrorResource)

    /**
     * @param t resource
     */
    fun onSuccess(resource: APIResource<V>)
}