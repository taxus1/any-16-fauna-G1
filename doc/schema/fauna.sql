-- any-16-fauna · 野生动物监测站单业务线 · 建表 SQL
-- 字符集 utf8mb4，时区 Asia/Shanghai。create 阶段建好，模型只写业务代码，不碰建表。
-- 列名即契约：del_flag 由 @TableLogic 自动拼接（查询带 del_flag=0，删除置 1），
-- create_by/update_by/create_time/update_time 由 AutoFillMetaObjectHandler 自动填充，业务代码不要手写。
-- 主键 id 由应用侧雪花分配（IdType.INPUT），不依赖自增。

-- 1) 监测站
CREATE TABLE IF NOT EXISTS t_monitor_station (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    station_no  VARCHAR(32)  NOT NULL COMMENT '监测站编号，全局唯一（如 ST-2026-0001）',
    name        VARCHAR(128) NOT NULL COMMENT '监测站名称',
    level       VARCHAR(16)  NOT NULL COMMENT '层级 PROVINCIAL 省级 / MUNICIPAL 市级 / COUNTY 县级',
    region      VARCHAR(128) NOT NULL COMMENT '所在辖区',
    leader      VARCHAR(64)  DEFAULT NULL COMMENT '负责人',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    status      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE 运行 / SUSPENDED 停用 / CLOSED 关闭',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by   VARCHAR(64)  DEFAULT NULL,
    create_time DATETIME     DEFAULT NULL,
    update_by   VARCHAR(64)  DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_station_no (station_no),
    KEY idx_status (status),
    KEY idx_region (region)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测站';

-- 2) 监测点（样线/样点/红外相机位）
CREATE TABLE IF NOT EXISTS t_monitor_site (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    site_no     VARCHAR(32)  NOT NULL COMMENT '监测点编号，全局唯一（如 MP-2026-0001）',
    station_id  BIGINT       NOT NULL COMMENT '所属监测站 id（t_monitor_station.id）',
    site_type   VARCHAR(16)  NOT NULL COMMENT '类型 TRANSECT 样线 / SAMPLE_POINT 样点 / CAMERA 红外相机位',
    habitat     VARCHAR(16)  NOT NULL COMMENT '生境 FOREST 林地 / WETLAND 湿地 / GRASSLAND 草地 / FARMLAND 农田 / DESERT 荒漠',
    location    VARCHAR(255) DEFAULT NULL COMMENT '位置描述',
    status      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE 在册 / INACTIVE 停测',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by   VARCHAR(64)  DEFAULT NULL,
    create_time DATETIME     DEFAULT NULL,
    update_by   VARCHAR(64)  DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_site_no (site_no),
    KEY idx_station (station_id),
    KEY idx_status (status),
    KEY idx_habitat (habitat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测点';

-- 3) 物种名录
CREATE TABLE IF NOT EXISTS t_species (
    id               BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    species_code     VARCHAR(32)  NOT NULL COMMENT '物种编码，全局唯一（如 SP-0001）',
    name             VARCHAR(64)  NOT NULL COMMENT '中文名',
    latin_name       VARCHAR(128) DEFAULT NULL COMMENT '学名',
    protection_level VARCHAR(16)  NOT NULL DEFAULT 'COMMON' COMMENT '保护级别 NATIONAL_ONE 国家一级 / NATIONAL_TWO 国家二级 / PROVINCIAL 省级 / COMMON 一般',
    status           VARCHAR(16)  NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED 启用 / DISABLED 停用',
    del_flag         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by        VARCHAR(64)  DEFAULT NULL,
    create_time      DATETIME     DEFAULT NULL,
    update_by        VARCHAR(64)  DEFAULT NULL,
    update_time      DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_species_code (species_code),
    KEY idx_status (status),
    KEY idx_protection (protection_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物种名录';

-- 4) 巡护任务
CREATE TABLE IF NOT EXISTS t_patrol_task (
    id             BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    task_no        VARCHAR(32)  NOT NULL COMMENT '巡护任务编号，全局唯一（如 PT-2026-0001）',
    station_id     BIGINT       NOT NULL COMMENT '所属监测站 id（t_monitor_station.id）',
    site_id        BIGINT       NOT NULL COMMENT '监测点 id（t_monitor_site.id）',
    patrol_type    VARCHAR(16)  NOT NULL DEFAULT 'ROUTINE' COMMENT '类型 ROUTINE 常规 / SPECIAL 专项 / EMERGENCY 应急',
    planned_date   DATE         NOT NULL COMMENT '计划日期',
    executor       VARCHAR(64)  DEFAULT NULL COMMENT '执行人',
    status         VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING 待执行 / IN_PROGRESS 执行中 / DONE 已完成 / CANCELLED 已取消',
    obs_count      INT          NOT NULL DEFAULT 0 COMMENT '该任务下观测记录条数（完成时汇总回写）',
    abnormal_count INT          NOT NULL DEFAULT 0 COMMENT '该任务下异常个体条数（完成时汇总回写）',
    started_at     DATETIME     DEFAULT NULL COMMENT '开始执行时刻',
    finished_at    DATETIME     DEFAULT NULL COMMENT '完成时刻',
    del_flag       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by      VARCHAR(64)  DEFAULT NULL,
    create_time    DATETIME     DEFAULT NULL,
    update_by      VARCHAR(64)  DEFAULT NULL,
    update_time    DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_task_no (task_no),
    KEY idx_station (station_id),
    KEY idx_site (site_id),
    KEY idx_status (status),
    KEY idx_planned (planned_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡护任务';

-- 5) 野生动物观测记录
CREATE TABLE IF NOT EXISTS t_wildlife_obs (
    id               BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    obs_no           VARCHAR(32)  NOT NULL COMMENT '观测编号，全局唯一（如 WO-2026-000001）',
    task_id          BIGINT       NOT NULL COMMENT '巡护任务 id（t_patrol_task.id）',
    site_id          BIGINT       NOT NULL COMMENT '监测点 id（t_monitor_site.id）',
    species_code     VARCHAR(32)  NOT NULL COMMENT '物种编码（t_species.species_code）',
    protection_level VARCHAR(16)  NOT NULL DEFAULT 'COMMON' COMMENT '观测当时保护级别快照',
    individual_count INT          NOT NULL DEFAULT 1 COMMENT '个体数量',
    health_status    VARCHAR(16)  NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL 正常 / INJURED 受伤 / DEAD 死亡 / SUSPECT 疑似疫病',
    observed_at      DATETIME     DEFAULT NULL COMMENT '观测时刻',
    recorder         VARCHAR(64)  DEFAULT NULL COMMENT '记录人',
    del_flag         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by        VARCHAR(64)  DEFAULT NULL,
    create_time      DATETIME     DEFAULT NULL,
    update_by        VARCHAR(64)  DEFAULT NULL,
    update_time      DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_obs_no (obs_no),
    KEY idx_task (task_id),
    KEY idx_site (site_id),
    KEY idx_species (species_code),
    KEY idx_health (health_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='野生动物观测记录';

-- 6) 异常个体上报
CREATE TABLE IF NOT EXISTS t_abnormal_report (
    id          BIGINT      NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    report_no   VARCHAR(32) NOT NULL COMMENT '上报编号，全局唯一（如 AR-2026-0001）',
    obs_id      BIGINT      NOT NULL COMMENT '观测记录 id（t_wildlife_obs.id）',
    site_id     BIGINT      NOT NULL COMMENT '监测点 id（t_monitor_site.id）',
    category    VARCHAR(16) NOT NULL DEFAULT 'INJURED' COMMENT '类别 INJURED 受伤 / DEAD 死亡 / SUSPECT_DISEASE 疑似疫病',
    severity    VARCHAR(16) NOT NULL DEFAULT 'LOW' COMMENT '严重程度 LOW 低 / MEDIUM 中 / HIGH 高',
    status      VARCHAR(16) NOT NULL DEFAULT 'REPORTED' COMMENT 'REPORTED 已上报 / HANDLING 处置中 / RESCUED 已救护 / SAMPLED 已采样 / CLOSED 已结案',
    reported_at DATETIME    DEFAULT NULL COMMENT '上报时刻',
    del_flag    TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by   VARCHAR(64) DEFAULT NULL,
    create_time DATETIME    DEFAULT NULL,
    update_by   VARCHAR(64) DEFAULT NULL,
    update_time DATETIME    DEFAULT NULL,
    UNIQUE KEY uk_report_no (report_no),
    KEY idx_obs (obs_id),
    KEY idx_site (site_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常个体上报';

-- 7) 采样送检与检测
CREATE TABLE IF NOT EXISTS t_sample_test (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    sample_no   VARCHAR(32)  NOT NULL COMMENT '样本编号，全局唯一（如 SM-2026-0001）',
    report_id   BIGINT       NOT NULL COMMENT '异常上报 id（t_abnormal_report.id）',
    sample_type VARCHAR(16)  NOT NULL COMMENT '样本类型 BLOOD 血液 / SWAB 拭子 / TISSUE 组织 / FECES 粪便',
    sent_at     DATETIME     DEFAULT NULL COMMENT '送检时刻',
    lab_name    VARCHAR(128) DEFAULT NULL COMMENT '检测机构',
    test_item   VARCHAR(64)  DEFAULT NULL COMMENT '检测项目',
    result      VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING 待检 / POSITIVE 阳性 / NEGATIVE 阴性 / INCONCLUSIVE 不确定',
    tested_at   DATETIME     DEFAULT NULL COMMENT '检测时刻',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by   VARCHAR(64)  DEFAULT NULL,
    create_time DATETIME     DEFAULT NULL,
    update_by   VARCHAR(64)  DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_sample_no (sample_no),
    KEY idx_report (report_id),
    KEY idx_result (result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采样送检与检测';

-- 8) 疫病预警与处置
CREATE TABLE IF NOT EXISTS t_epi_alert (
    id              BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    alert_no        VARCHAR(32)  NOT NULL COMMENT '预警编号，全局唯一（如 AL-2026-0001）',
    report_id       BIGINT       NOT NULL COMMENT '异常上报 id（t_abnormal_report.id）',
    sample_id       BIGINT       NOT NULL COMMENT '阳性样本 id（t_sample_test.id）',
    alert_level     VARCHAR(16)  NOT NULL COMMENT '预警级别 BLUE 蓝色 / YELLOW 黄色 / ORANGE 橙色 / RED 红色',
    status          VARCHAR(16)  NOT NULL DEFAULT 'RAISED' COMMENT 'RAISED 已发布 / HANDLING 处置中 / RESOLVED 已解除 / CLOSED 已归档',
    disposal_method VARCHAR(64)  DEFAULT NULL COMMENT '处置措施',
    raised_at       DATETIME     DEFAULT NULL COMMENT '预警发布时刻',
    resolved_at     DATETIME     DEFAULT NULL COMMENT '解除时刻',
    del_flag        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by       VARCHAR(64)  DEFAULT NULL,
    create_time     DATETIME     DEFAULT NULL,
    update_by       VARCHAR(64)  DEFAULT NULL,
    update_time     DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_alert_no (alert_no),
    KEY idx_report (report_id),
    KEY idx_sample (sample_id),
    KEY idx_level (alert_level),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疫病预警与处置';
