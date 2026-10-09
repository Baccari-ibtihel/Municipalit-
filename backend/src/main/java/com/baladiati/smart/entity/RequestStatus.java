package com.baladiati.smart.entity;

public enum RequestStatus {
    SENT,        // تم إرسال الطلب
    VERIFYING,   // جاري التثبت من الوثائق
    INCOMPLETE,  // الوثائق ناقصة
    PROCESSED,   // تمت معالجة الطلب
    READY        // الوثيقة جاهزة للاستلام
}
