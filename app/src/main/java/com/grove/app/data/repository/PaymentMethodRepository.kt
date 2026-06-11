package com.grove.app.data.repository

import com.grove.app.data.db.dao.PaymentMethodDao
import com.grove.app.data.model.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PaymentMethodRepository(
    private val dao: PaymentMethodDao,
) {
    fun observeActive(): Flow<List<PaymentMethod>> = dao.observeActive().map { list -> list.map { it.toDomain() } }
}
