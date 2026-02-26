package org.shanguanling.project;

enum CarState {
    None,           // 未入场，未放行
    ENTERED,        // 已入场，未放行
    RELEASED,       // 已入场，等待放行
    EXITED          // 已入场，已放行
}