package com.grove.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Entity-list to domain-list mapping shared by every repository's observe methods. */
internal fun <E, D> Flow<List<E>>.mapList(transform: (E) -> D): Flow<List<D>> =
    map { list -> list.map(transform) }
