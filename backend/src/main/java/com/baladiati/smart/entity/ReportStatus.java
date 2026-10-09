package com.baladiati.smart.entity;

public enum ReportStatus {
    RECEIVED,        // تم استقبال البلاغ
    INVESTIGATING,   // جاري المعاينة
    IN_PROGRESS,     // قيد الإصلاح
    TEAM_DISPATCHED, // تم إرسال فريق التدخل
    SOLVED           // تم حل المشكلة
}
