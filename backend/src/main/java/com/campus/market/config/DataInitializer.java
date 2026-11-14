package com.campus.market.config;

import com.campus.market.mapper.UserMapper;
import com.campus.market.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 首次启动时写入演示数据（分类 / 用户 / 商品）。
 * 数据库中已有用户时不再重复初始化。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;

    @Override
    public void run(String... args) {
        if (userMapper.selectCount(null) > 0) {
            log.info("检测到已有数据，跳过演示数据初始化");
            return;
        }

        initCategories();
        initUsers();
        initItems();
        log.info("演示数据初始化完成：演示账号 demo / 123456");
    }

    private void initCategories() {
        String[][] categories = {
                {"数码电子", "💻", "1"},
                {"图书教材", "📚", "2"},
                {"生活用品", "🛍️", "3"},
                {"运动器材", "🏀", "4"},
                {"美妆服饰", "👕", "5"},
                {"其他闲置", "✨", "6"},
        };
        for (String[] c : categories) {
            jdbcTemplate.update("INSERT INTO t_category (name, icon, sort) VALUES (?, ?, ?)",
                    c[0], c[1], Integer.parseInt(c[2]));
        }
    }

    private void initUsers() {
        Object[][] users = {
                {"demo", "陈同学", "嘉庚学院 · 软件工程", "热爱折腾数码，闲置都在这出。"},
                {"linxm", "林小满", "嘉庚学院 · 会计学", "书多，价格好说，支持当面交易。"},
                {"zhouza", "周子昂", "嘉庚学院 · 机械设计", "运动装备爱好者，毕业清仓中。"},
                {"suqing", "苏晴", "嘉庚学院 · 视觉传达", "衣服都是自己穿过一次的，干净。"},
        };
        for (Object[] u : users) {
            jdbcTemplate.update(
                    "INSERT INTO t_user (username, password, nickname, intro, role, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                    u[0], PasswordUtil.encode("123456"), u[1], u[2], "USER",
                    Timestamp.valueOf(LocalDateTime.now().minusDays(30)));
        }
    }

    private void initItems() {
        // title, description, price, originalPrice, categoryId, sellerId, condition, location, daysAgo, viewCount
        Object[][] items = {
                {"iPad Air 4 64G 深空灰 九成新",
                        "去年买的，一直贴膜带壳用，屏幕无划痕，电池健康 92%。送一支原装笔和磁吸保护壳，当面验机。",
                        "2199", "4399", 1L, 2L, "几乎全新", "嘉庚学院 · 图书馆门口", 1, 126},
                {"《计算机网络：自顶向下方法》第 8 版",
                        "课程用书，翻过前三章，笔记用铅笔写的，可以擦掉。附课后题答案打印版。",
                        "35", "89", 2L, 3L, "轻微使用", "嘉庚学院 · 学生宿舍 3 栋", 2, 58},
                {"宿舍静音小冰箱 45L",
                        "搬宿舍用不上了，制冷正常，声音很小，放宿舍完全没存在感。自提，可帮搬到楼下。",
                        "180", "399", 3L, 2L, "轻微使用", "嘉庚学院 · 学生宿舍 6 栋", 3, 210},
                {"耐克篮球 7 号 室外耐磨款",
                        "打了三个月室内场地，纹路还在，气足。送一个便携气筒。",
                        "60", "129", 4L, 4L, "轻微使用", "嘉庚学院 · 体育馆", 4, 74},
                {"优衣库 oversize 卫衣 M 码 燕麦色",
                        "只穿过两次，洗过一次，无起球。身高 170-180 都能穿，偏宽松。",
                        "45", "199", 5L, 3L, "几乎全新", "嘉庚学院 · 女生宿舍楼", 5, 41},
                {"罗技 G304 无线游戏鼠标",
                        "换新鼠标了所以出，键程正常，微动无双击，含 USB 无线接收器。",
                        "130", "199", 1L, 4L, "几乎全新", "嘉庚学院 · 教学楼 B 区", 6, 96},
                {"小米台灯 Pro 护眼款",
                        "宿舍用了一个学期，无频闪，可调色温。搬回家了所以出掉。",
                        "55", "169", 3L, 2L, "轻微使用", "嘉庚学院 · 学生宿舍 6 栋", 7, 33},
                {"《高等数学》同济第八版 上下册",
                        "全新未拆封，买重复了。两本一起出，不单卖。",
                        "25", "78", 2L, 4L, "全新", "嘉庚学院 · 快递站", 8, 27},
                {"迪卡依瑜伽垫 183×61cm 加厚",
                        "买来练了几次，已擦洗消毒，配收纳绑带。",
                        "40", "79", 4L, 3L, "几乎全新", "嘉庚学院 · 操场", 9, 18},
                {"负离子电吹风 大风量",
                        "三档风速，宿舍可用，线长 1.8 米。外观有轻微使用痕迹，功能完好。",
                        "40", "99", 3L, 4L, "明显使用", "嘉庚学院 · 学生宿舍 5 栋", 10, 52},
                {"汉服 齐胸襍裙 均码 难蓝",
                        "拍过一次写真就压箱底了，清洗过，配发包和披帛。",
                        "88", "268", 5L, 3L, "几乎全新", "嘉庚学院 · 学生活动中心", 11, 66},
                {"机械键盘 87 键 红轴 白色",
                        "手感很好，打字声音不大，键帽无打油。带原装数据线和拔键器。",
                        "150", "349", 1L, 2L, "轻微使用", "嘉庚学院 · 图书馆门口", 12, 143},
        };

        for (Object[] it : items) {
            Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now().minusDays((Integer) it[8]));
            jdbcTemplate.update(
                    "INSERT INTO t_item (title, description, price, original_price, category_id, seller_id, "
                            + "item_condition, location, status, view_count, created_at, updated_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ?)",
                    it[0], it[1], new BigDecimal((String) it[2]), new BigDecimal((String) it[3]),
                    it[4], it[5], it[6], it[7], it[9], createdAt, createdAt);
        }
    }
}
