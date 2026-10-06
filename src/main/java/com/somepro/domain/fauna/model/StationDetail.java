package com.somepro.domain.fauna.model;

/**
 * 监测站详情（领域读模型，不可变值对象）。
 *
 * 在站档案之外，额外带上名下点位的在册数 / 停测数。
 * 这两个数直接按 t_monitor_site 的同一份状态口径 COUNT 出来，
 * 与监测点模块翻出来的记录对得上 —— 不另存冗余计数，就不会「两边各显示一个数」。
 */
public record StationDetail(MonitorStation station, long activeSiteCount, long inactiveSiteCount) {
}
