package com.grove.app.data.db

import androidx.room.TypeConverter
import com.grove.app.data.model.BillFrequency
import com.grove.app.data.model.CategoryKind
import com.grove.app.data.model.NotificationKind
import com.grove.app.data.model.PaymentKind
import java.time.Instant
import java.util.UUID

class Converters {
    @TypeConverter fun uuidToString(value: UUID?): String? = value?.toString()

    @TypeConverter fun stringToUuid(value: String?): UUID? = value?.let(UUID::fromString)

    @TypeConverter fun instantToLong(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter fun longToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter fun categoryKindToString(value: CategoryKind): String = value.name

    @TypeConverter fun stringToCategoryKind(value: String): CategoryKind = CategoryKind.valueOf(value)

    @TypeConverter fun billFrequencyToString(value: BillFrequency): String = value.name

    @TypeConverter fun stringToBillFrequency(value: String): BillFrequency = BillFrequency.valueOf(value)

    @TypeConverter fun paymentKindToString(value: PaymentKind): String = value.name

    @TypeConverter fun stringToPaymentKind(value: String): PaymentKind = PaymentKind.valueOf(value)

    @TypeConverter fun notificationKindToString(value: NotificationKind): String = value.name

    @TypeConverter fun stringToNotificationKind(value: String): NotificationKind = NotificationKind.valueOf(value)
}
