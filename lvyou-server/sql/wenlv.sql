/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80409
 Source Host           : localhost:3306
 Source Schema         : wenlv

 Target Server Type    : MySQL
 Target Server Version : 80409
 File Encoding         : 65001

 Date: 30/10/2026 13:48:10
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for banner
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '名称',
  `image_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '图片地址',
  `classify` int(0) NULL DEFAULT NULL COMMENT '分类1banner图 2金刚区分类 9境外banner',
  `url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '跳转地址 ',
  `sort` int(0) NULL DEFAULT NULL COMMENT '顺序',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '描述',
  `state` int(0) NULL DEFAULT NULL COMMENT '状态1正常 2隐藏',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'banner' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of banner
-- ----------------------------
INSERT INTO `banner` VALUES (1, '首页海报', 'https://tu.mianyazuju.com/2026/09/10/464539de-e4e5-4f75-ad4d-e097a3baf735.png', 1, NULL, 1, NULL, 1, '2026-09-10 10:56:59', '2026-09-16 10:15:24');

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '消息内容',
  `image_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '图片URL，逗号分隔',
  `url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '跳转链接',
  `type` tinyint(0) NOT NULL COMMENT '消息类型: 1=系统公告, 2=用户消息',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '接收用户ID',
  `data_id` bigint(0) UNSIGNED NULL DEFAULT NULL COMMENT '关联数据ID（如服务ID、订单ID等）',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读: 0=未读, 1=已读',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id_is_read`(`user_id`, `is_read`) USING BTREE COMMENT '用户消息状态查询索引',
  INDEX `idx_type_create_time`(`type`, `create_time`) USING BTREE COMMENT '按类型和时间查询索引',
  INDEX `idx_user_id_create_time`(`user_id`, `create_time`) USING BTREE COMMENT '用户消息时间线索引'
) ENGINE = InnoDB AUTO_INCREMENT = 60 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '消息信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of messages
-- ----------------------------
INSERT INTO `messages` VALUES (1, '订单已完成', '您的订单 RT20260907002 已完成', NULL, NULL, 2, 1, 2, 0, '2026-09-14 11:40:36', '2026-09-14 11:40:35');
INSERT INTO `messages` VALUES (2, '订单已完成', '您的订单 RT20260908001 已完成', NULL, NULL, 2, 1, 1, 0, '2026-09-14 11:40:39', '2026-09-14 11:40:39');
INSERT INTO `messages` VALUES (3, '支付成功', '订单 HT1789639481728115 已支付成功（余额支付）', NULL, NULL, 2, 2, 5, 1, '2026-09-17 18:04:43', '2026-09-18 13:49:01');
INSERT INTO `messages` VALUES (4, '退款申请已提交', '您的订单 HT1789639481728115 退款申请已提交，请等待处理', NULL, NULL, 2, 2, 5, 1, '2026-09-18 11:16:28', '2026-09-18 13:49:01');
INSERT INTO `messages` VALUES (5, '退款已同意', '您的订单 HT1789639481728115 退款已同意', NULL, NULL, 2, 2, 5, 1, '2026-09-18 11:17:55', '2026-09-18 13:49:01');
INSERT INTO `messages` VALUES (6, '订单已取消', '您的订单 HT1789704334238915 已取消', NULL, NULL, 2, 2, 7, 1, '2026-09-18 12:05:34', '2026-09-18 13:49:01');
INSERT INTO `messages` VALUES (7, '支付成功', '订单 HT1789704334409472 已支付成功（余额支付）', NULL, NULL, 2, 2, 8, 1, '2026-09-18 12:05:34', '2026-09-18 13:49:01');
INSERT INTO `messages` VALUES (8, '退款申请已提交', '您的订单 HT1789704334409472 退款申请已提交，请等待处理', NULL, NULL, 2, 2, 8, 1, '2026-09-18 12:05:35', '2026-09-18 13:34:27');
INSERT INTO `messages` VALUES (9, '退款已同意', '您的订单 HT1789704334409472 退款已同意', NULL, NULL, 2, 2, 8, 1, '2026-09-18 12:05:35', '2026-09-18 13:34:34');
INSERT INTO `messages` VALUES (10, '支付成功', '订单 HT1789704334658237 已支付成功（余额支付）', NULL, NULL, 2, 2, 9, 1, '2026-09-18 12:05:35', '2026-09-18 13:49:01');
INSERT INTO `messages` VALUES (11, '入住办理成功', '您的订单 HT1789704334658237 已办理入住', NULL, NULL, 2, 2, 9, 1, '2026-09-18 12:05:35', '2026-09-18 13:49:01');
INSERT INTO `messages` VALUES (12, '订单已完成', '您的订单 HT1789704334658237 已完成', NULL, NULL, 2, 2, 9, 1, '2026-09-18 12:05:35', '2026-09-18 13:49:01');
INSERT INTO `messages` VALUES (13, '支付成功', '订单 HT1789704334832832 已支付成功（余额支付）', NULL, NULL, 2, 2, 10, 1, '2026-09-18 12:05:35', '2026-09-18 13:49:01');
INSERT INTO `messages` VALUES (14, '订单已取消', '您的订单 HT1789704334832832 已取消并退款处理中', NULL, NULL, 2, 2, 10, 1, '2026-09-18 12:05:35', '2026-09-18 13:49:01');
INSERT INTO `messages` VALUES (15, '订单已取消', '您的订单 HT1789712348595767 已取消', NULL, NULL, 2, 2, 11, 1, '2026-09-18 14:23:35', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (16, '支付成功', '订单 HT1789714105261153 已支付成功（余额支付）', NULL, NULL, 2, 2, 12, 1, '2026-09-18 14:48:27', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (17, '订单已取消', '您的订单 HT1789714105261153 已取消并退款处理中', NULL, NULL, 2, 2, 12, 1, '2026-09-18 15:43:44', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (18, '订单已取消', '您的订单 HT1789717461338462 已取消', NULL, NULL, 2, 2, 13, 1, '2026-09-18 16:06:03', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (19, '支付成功', '订单 HT1789718796601712 已支付成功（余额支付）', NULL, NULL, 2, 2, 14, 1, '2026-09-18 16:06:38', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (20, '入住办理成功', '您的订单 HT1789718796601712 已办理入住', NULL, NULL, 2, 2, 14, 1, '2026-09-18 16:06:57', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (21, '订单已完成', '您的酒店订单 HT1789718796601712 已离店完成，欢迎评价', NULL, NULL, 2, 2, 14, 1, '2026-09-20 09:25:00', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (22, '支付成功', '订单 HT1789867689344245 已支付成功（余额支付）', NULL, NULL, 2, 2, 15, 1, '2026-09-20 09:28:11', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (23, '入住办理成功', '您的订单 HT1789867689344245 已办理入住', NULL, NULL, 2, 2, 15, 1, '2026-09-20 09:28:32', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (24, '线路预约提交成功', '您的线路预约 RT1790043693213814 已提交，等待商家确认', NULL, NULL, 2, 2, 16, 1, '2026-09-22 10:21:33', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (25, '订单已确认', '您的订单 RT1790043693213814 已确认', NULL, NULL, 2, 2, 16, 1, '2026-09-22 13:13:35', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (26, '订单已完成', '您的订单 RT1790043693213814 已完成', NULL, NULL, 2, 2, 16, 1, '2026-09-22 13:13:39', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (27, '餐饮预约提交成功', '您的餐饮预约 CR202609230930402991 已提交，等待确认', NULL, NULL, 2, 2, 1, 1, '2026-09-23 09:30:40', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (28, '支付成功', '订单 RT1790131563650207 已支付成功（余额支付）', NULL, NULL, 2, 2, 17, 1, '2026-09-23 11:02:30', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (29, '支付成功', '订单 RT1790133846842435 已支付成功（余额支付）', NULL, NULL, 2, 2, 18, 1, '2026-09-23 11:24:08', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (30, '餐饮预约提交成功', '您的餐饮预约 CR202609231420264447 已提交，等待确认', NULL, NULL, 2, 3, 2, 1, '2026-09-23 14:20:26', '2026-09-23 19:58:03');
INSERT INTO `messages` VALUES (31, '餐饮预约已修改', '您的餐饮预约 CR202609231420264447 信息已更新', NULL, NULL, 2, 3, 2, 1, '2026-09-23 14:30:39', '2026-09-23 19:58:03');
INSERT INTO `messages` VALUES (32, '餐饮预约已取消', '您的餐饮预约 CR202609231420264447 已取消', NULL, NULL, 2, 3, 2, 1, '2026-09-23 14:33:04', '2026-09-23 19:58:03');
INSERT INTO `messages` VALUES (33, '餐饮预约提交成功', '您的餐饮预约 CR202609231433185667 已提交，等待确认', NULL, NULL, 2, 3, 3, 1, '2026-09-23 14:33:19', '2026-09-23 19:58:03');
INSERT INTO `messages` VALUES (34, '餐饮预约提交成功', '您的餐饮预约 CR202609231523585366 已提交，等待确认', NULL, NULL, 2, 2, 4, 1, '2026-09-23 15:23:59', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (35, '餐饮预约提交成功', '您的餐饮预约 CR202609231526355180 已提交，等待确认', NULL, NULL, 2, 2, 5, 1, '2026-09-23 15:26:35', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (36, '餐饮预约提交成功', '您的餐饮预约 CR202609231526354618 已提交，等待确认', NULL, NULL, 2, 2, 6, 1, '2026-09-23 15:26:35', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (37, '餐饮预约已确认', '您的餐饮预约 CR202609231526354618 已确认', NULL, NULL, 2, 2, 6, 1, '2026-09-23 15:54:03', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (38, '订单已确认', '您的订单 RT202609231547450006 已确认', NULL, NULL, 2, 2, 23, 1, '2026-09-23 15:54:03', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (39, '餐饮预约提交成功', '您的餐饮预约 CR202609231620525572 已提交，等待确认', NULL, NULL, 2, 3, 7, 1, '2026-09-23 16:20:53', '2026-09-23 19:58:03');
INSERT INTO `messages` VALUES (40, '新餐饮预约', '叁拾 预约了 十八陕菜馆（2人，2026-09-23 12:00），请及时确认', NULL, NULL, 2, 4, 7, 0, '2026-09-23 16:20:53', '2026-09-23 16:20:52');
INSERT INTO `messages` VALUES (41, '餐饮预约已确认', '您的餐饮预约 CR202609231620525572 已确认', NULL, NULL, 2, 3, 7, 1, '2026-09-23 16:49:55', '2026-09-23 19:58:03');
INSERT INTO `messages` VALUES (42, '订单已确认', '您的订单 RT202609231620528995 已确认', NULL, NULL, 2, 3, 26, 1, '2026-09-23 16:49:55', '2026-09-23 19:58:03');
INSERT INTO `messages` VALUES (43, '餐饮预约已完成', '您的餐饮预约 CR202609231620525572 已完成', NULL, NULL, 2, 3, 7, 1, '2026-09-23 16:50:25', '2026-09-23 19:58:03');
INSERT INTO `messages` VALUES (44, '订单已完成', '您的订单 RT202609231620528995 已完成', NULL, NULL, 2, 3, 26, 1, '2026-09-23 16:50:25', '2026-09-23 19:58:03');
INSERT INTO `messages` VALUES (45, '支付成功', '订单 OD1790227052165352 已支付成功（余额支付）', NULL, NULL, 2, 4, 27, 0, '2026-09-24 13:17:34', '2026-09-24 13:17:33');
INSERT INTO `messages` VALUES (46, '支付成功', '订单 OD1790227447374118 已支付成功（余额支付）', NULL, NULL, 2, 4, 28, 0, '2026-09-24 13:24:08', '2026-09-24 13:24:08');
INSERT INTO `messages` VALUES (47, '订单已发货', '您的订单 OD1790227447374118 已发货', NULL, NULL, 2, 4, 28, 0, '2026-09-24 13:45:04', '2026-09-24 13:45:04');
INSERT INTO `messages` VALUES (48, '退款申请已提交', '您的订单 OD1790227447374118 退款申请已提交，请等待处理', NULL, NULL, 2, 4, 28, 0, '2026-09-24 15:26:15', '2026-09-24 15:26:15');
INSERT INTO `messages` VALUES (49, '退款已同意', '您的订单 OD1790227447374118 退款已同意', NULL, NULL, 2, 4, 28, 0, '2026-09-24 15:26:26', '2026-09-24 15:26:25');
INSERT INTO `messages` VALUES (50, '订单已发货', '您的订单 OD1790227052165352 已发货', NULL, NULL, 2, 4, 27, 0, '2026-09-24 15:26:44', '2026-09-24 15:26:43');
INSERT INTO `messages` VALUES (51, '确认收货成功', '您的订单 OD1790227052165352 已确认收货', NULL, NULL, 2, 4, 27, 0, '2026-09-24 15:28:11', '2026-09-24 15:28:11');
INSERT INTO `messages` VALUES (52, '支付成功', '订单 OD1790234904661588 已支付成功（余额支付）', NULL, NULL, 2, 4, 29, 0, '2026-09-24 15:28:27', '2026-09-24 15:28:26');
INSERT INTO `messages` VALUES (53, '订单已发货', '您的订单 OD1790234904661588 已发货', NULL, NULL, 2, 4, 29, 0, '2026-09-24 15:28:32', '2026-09-24 15:28:32');
INSERT INTO `messages` VALUES (54, '退款申请已提交', '您的订单 OD1790234904661588 退款申请已提交，请等待处理', NULL, NULL, 2, 4, 29, 0, '2026-09-24 15:28:54', '2026-09-24 15:28:53');
INSERT INTO `messages` VALUES (55, '支付成功', '订单 OD1790234942539774 已支付成功（余额支付）', NULL, NULL, 2, 4, 30, 0, '2026-09-24 15:29:04', '2026-09-24 15:29:03');
INSERT INTO `messages` VALUES (56, '支付成功', '订单 OD1790236304667593 已支付成功（余额支付）', NULL, NULL, 2, 2, 31, 1, '2026-09-24 15:51:46', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (57, '退款申请已提交', '您的订单 OD1790236304667593 退款申请已提交，请等待处理', NULL, NULL, 2, 2, 31, 1, '2026-09-24 15:52:40', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (58, '支付成功', '订单 OD1790236377798864 已支付成功（余额支付）', NULL, NULL, 2, 2, 32, 1, '2026-09-24 15:52:59', '2026-09-24 16:59:09');
INSERT INTO `messages` VALUES (59, '订单已取消', '订单 OD1790236387743856 因支付超时已自动取消', NULL, NULL, 2, 2, 33, 1, '2026-09-24 16:30:00', '2026-09-24 16:59:09');

-- ----------------------------
-- Table structure for sms_template
-- ----------------------------
DROP TABLE IF EXISTS `sms_template`;
CREATE TABLE `sms_template`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `biz_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `template_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sms_template_biz_code`(`biz_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '短信模板' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_template
-- ----------------------------
INSERT INTO `sms_template` VALUES (1, 'register', NULL, '【%s】您的验证码是%s，10分钟内有效', '2026-07-17 11:11:13', '2026-07-17 11:11:13');
INSERT INTO `sms_template` VALUES (2, 'login', NULL, '【%s】您的验证码是%s，10分钟内有效', '2026-07-17 11:11:13', '2026-07-17 11:11:13');
INSERT INTO `sms_template` VALUES (3, 'forget', NULL, '【%s】您的验证码是%s，10分钟内有效', '2026-07-17 11:11:13', '2026-07-17 11:11:13');

-- ----------------------------
-- Table structure for sys_captcha
-- ----------------------------
DROP TABLE IF EXISTS `sys_captcha`;
CREATE TABLE `sys_captcha`  (
  `uuid` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'uuid',
  `code` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '验证码',
  `expire_time` datetime(0) NULL DEFAULT NULL COMMENT '过期时间',
  PRIMARY KEY (`uuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '系统验证码' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_captcha
-- ----------------------------
INSERT INTO `sys_captcha` VALUES ('6b4b838a-7f07-4510-9961-91faf74eccc8', '87fxf', '2026-09-18 16:22:25');
INSERT INTO `sys_captcha` VALUES ('9c7c6bf5-e4a4-4aeb-b7e1-e8a22b9a0dad', 'f2cf5', '2026-09-15 09:31:08');
INSERT INTO `sys_captcha` VALUES ('a9d375cd-0ae3-4288-921e-705cae700110', '82dxy', '2026-09-10 10:10:35');
INSERT INTO `sys_captcha` VALUES ('d003c003-8740-4d68-a178-09250b18b804', 'pynb7', '2026-09-15 09:15:42');
INSERT INTO `sys_captcha` VALUES ('d202ccbf-e474-4d53-a1b7-678728178441', 'g463f', '2026-09-16 13:19:04');
INSERT INTO `sys_captcha` VALUES ('dcd92451-3bfc-4f0a-a81c-126e44f6aee1', 'c34n2', '2026-09-10 10:12:17');
INSERT INTO `sys_captcha` VALUES ('ea61adef-9eca-4c2a-9b61-f9d251304eda', 'w5bgn', '2026-09-10 10:16:23');

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `param_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'key',
  `param_value` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'value',
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '状态   0：隐藏   1：显示',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `param_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '类型',
  `param_type_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '类型描述',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `param_key`(`param_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1062 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '系统配置信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 'file_storage_type', 'ali', 1, '文件存储类型：local=本地，ali=阿里云 OSS', 'file', '文件配置');
INSERT INTO `sys_config` VALUES (2, 'file_local_upload_path', '/upload', 1, '本地上传目录（file_storage_type=local 时使用）', 'file', '文件配置');
INSERT INTO `sys_config` VALUES (3, 'sms_platform', '3', 1, '短信平台 1:腾讯云 2: 阿里云 3: 短信宝', 'sms', '短信配置');
INSERT INTO `sys_config` VALUES (4, 'sms_smsbao_username', '', 1, '短信宝用户名', 'sms', '短信配置');
INSERT INTO `sys_config` VALUES (5, 'sms_smsbao_password', '', 1, '短信宝密码', 'sms', '短信配置');
INSERT INTO `sys_config` VALUES (6, 'sms_ali_access_key', NULL, 1, '阿里云短信accessKeyId', 'sms', '短信配置');
INSERT INTO `sys_config` VALUES (7, 'sms_ali_access_secret', NULL, 1, '阿里云短信accessSecret', 'sms', '短信配置');
INSERT INTO `sys_config` VALUES (8, 'sys_server_name', '省钱兄文旅', 1, '产品名称（用户端与管理端展示，亦用于琭信/邮件签名）', 'sys', '系统配置');
INSERT INTO `sys_config` VALUES (9, 'sms_tencent_client_id', NULL, 1, '腾讯云短信clientId', 'sms', '短信配置');
INSERT INTO `sys_config` VALUES (10, 'sms_tencent_client_secret', NULL, 1, '腾讯云短信clientSecret', 'sms', '短信配置');
INSERT INTO `sys_config` VALUES (11, 'sys_h5_domain', 'https://lvyouwl.xianmaxiong.com', 1, '用户端 H5 域名', 'sys', '域名');
INSERT INTO `sys_config` VALUES (12, 'sys_admin_domain', '', 1, '后台管理平台域名配置', 'sys', '系统配置');
INSERT INTO `sys_config` VALUES (18, 'pay_ali_appid', '', 1, '支付宝 AppId（开放平台应用）', 'pay', '支付宝');
INSERT INTO `sys_config` VALUES (19, 'pay_ali_public_key', '', 1, '支付宝公钥（公钥模式 pay_ali_mode=1 时必填）', 'pay', '支付宝');
INSERT INTO `sys_config` VALUES (20, 'pay_ali_private_key', '', 1, '支付宝应用私钥（RSA2，一行字符串，不含头尾）', 'pay', '支付宝');
INSERT INTO `sys_config` VALUES (28, 'sys_wechat_app_appid', '', 1, '微信 App AppId（支付模块）', 'pay', '微信支付');
INSERT INTO `sys_config` VALUES (29, 'pay_wechat_mch_key', '', 1, '微信支付商户密钥（本地联调占位）', 'pay', '微信支付');
INSERT INTO `sys_config` VALUES (30, 'pay_wechat_mch_id', '', 1, '微信支付商户号（本地联调占位）', 'pay', '微信支付');
INSERT INTO `sys_config` VALUES (31, 'sys_wechat_h5_appid', '', 1, '微信 H5/公众号 AppId（支付模块）', 'pay', '微信支付');
INSERT INTO `sys_config` VALUES (32, 'sys_wechat_jsapi_appid', '', 1, '微信 JSAPI/小程序 AppId（支付模块）', 'pay', '微信支付');
INSERT INTO `sys_config` VALUES (33, 'mail_host', '', 1, '邮箱服务器地址', 'mail', '邮件配置');
INSERT INTO `sys_config` VALUES (34, 'mail_send_mail', '', 1, '发送信息邮箱', 'mail', '邮件配置');
INSERT INTO `sys_config` VALUES (35, 'mail_port', '', 1, '邮箱端口号', 'mail', '邮件配置');
INSERT INTO `sys_config` VALUES (36, 'mail_auth_code', '', 1, '邮箱授权码', 'mail', '邮件配置');
INSERT INTO `sys_config` VALUES (39, 'file_bt_endpoint', '', 1, '宝塔上传地址', 'file', '文件配置');
INSERT INTO `sys_config` VALUES (41, 'sys_domain', 'https://lvyouwl.xianmaxiong.com', 1, '用户端业务域名', 'sys', '域名');
INSERT INTO `sys_config` VALUES (46, 'pay_wx_h5_app_id', '', 1, '微信 H5/公众号支付 AppId', 'pay', '微信支付');
INSERT INTO `sys_config` VALUES (47, 'pay_wx_jsapi_app_id', '', 1, '微信小程序/JSAPI 支付 AppId', 'pay', '微信支付');
INSERT INTO `sys_config` VALUES (50, 'file_endpoint', '', 1, '阿里云 OSS Endpoint', 'file', '文件配置');
INSERT INTO `sys_config` VALUES (51, 'file_bucket', '', 1, '阿里云 OSS Bucket', 'file', '文件配置');
INSERT INTO `sys_config` VALUES (52, 'file_ali_oss_accessKeyId', '', 1, '阿里云 OSS AccessKeyId', 'file', '文件配置');
INSERT INTO `sys_config` VALUES (53, 'file_ali_oss_secretAccessKey', '', 1, '阿里云 OSS SecretAccessKey', 'file', '文件配置');
INSERT INTO `sys_config` VALUES (54, 'pay_ali_app_cert_path', '', 1, '应用公钥证书路径（证书模式 pay_ali_mode=2）', 'pay', '支付宝');
INSERT INTO `sys_config` VALUES (59, 'pay_ali_alipay_public_cert_path', '', 1, '支付宝公钥证书路径（证书模式）', 'pay', '支付宝');
INSERT INTO `sys_config` VALUES (60, 'pay_ali_root_cert_path', '', 1, '支付宝根证书路径（证书模式）', 'pay', '支付宝');
INSERT INTO `sys_config` VALUES (61, 'pay_ali_mode', '1', 1, '支付宝支付模式：1=公钥模式，2=证书模式', 'pay', '支付宝');
INSERT INTO `sys_config` VALUES (62, 'pay_ali_sandbox', '2', 1, '支付宝沙箱：1=沙箱环境，2=正式环境', 'pay', '支付宝');
INSERT INTO `sys_config` VALUES (63, 'map_type', '1', 1, '地图类型 1: 腾讯地图 2：天地图 3：谷歌地图', 'map', '地图配置');
INSERT INTO `sys_config` VALUES (64, 'google_map_key', 'AIzaSyDXVZG8xz9fpRQYq7QFyxiU7AD3Wsr7a9s', 1, '谷歌地图key', 'map', '地图配置');
INSERT INTO `sys_config` VALUES (67, 'sys_wx_key_path', '', 1, '微信支付证书路径（本地联调占位）', 'pay', '微信支付');
INSERT INTO `sys_config` VALUES (68, 'sys_wechat_jsapi_secret', '', 1, '微信 JSAPI/小程序 Secret', 'map', '地图配置');
INSERT INTO `sys_config` VALUES (69, 'sys_wechat_h5_secret', '', 1, '微信公众号秘钥', 'map', '地图配置');
INSERT INTO `sys_config` VALUES (76, 'prot_user', '<h3>用户服务协议</h3><p>荣e游用户服务协议，待完善。</p>', 1, '用户协议', 'content', '协议文案');
INSERT INTO `sys_config` VALUES (77, 'prot_privacy', '<h3>隐私政策</h3><p>荣e游隐私政策，待完善。</p>', 1, '隐私政策', 'content', '协议文案');
INSERT INTO `sys_config` VALUES (78, 'sys_about_me', '<h3>关于我们</h3><p>荣e游 — 荣昌文旅服务平台。</p>', 1, '关于我们', 'content', '协议文案');
INSERT INTO `sys_config` VALUES (79, 'sys_customer_qrcode', 'https://tu.mianyazuju.com/20251218/3128e79fc3a14dd6b21951167311e232.png', 1, '客服二维码', 'content', '客服');
INSERT INTO `sys_config` VALUES (80, 'sys_customer_wx_number', 'mock_service_2026', 1, '客服微信号', 'content', '客服');
INSERT INTO `sys_config` VALUES (84, 'sys_user_ma_appid', '', 1, '微信小程序AppId', 'sys', '微信');
INSERT INTO `sys_config` VALUES (85, 'sys_user_ma_secret', '', 1, '微信小程序Secret', 'sys', '微信');
INSERT INTO `sys_config` VALUES (86, 'sys_user_mp_appid', '', 1, '微信公众号AppId', 'sys', '微信');
INSERT INTO `sys_config` VALUES (87, 'sys_user_mp_secret', '', 1, '微信公众号Secret', 'sys', '微信');
INSERT INTO `sys_config` VALUES (823, 'sms_provider', '3', 1, '短信平台：mock=本地联调，3=短信宝，2=阿里云，1=腾讯云', 'sys', '短信');
INSERT INTO `sys_config` VALUES (898, 'file_public_base_url', 'https://lvyouwl.xianmaxiong.com/sqx_fast', 1, '公网 API 前缀（支付回调）', 'sys', '域名');
INSERT INTO `sys_config` VALUES (899, 'file_cdn_domain', 'https://tu.mianyazuju.com', 1, 'OSS 静态 CDN 域名（file_storage_type=ali 时使用）', 'sys', '文件');
INSERT INTO `sys_config` VALUES (908, 'sys_customer_mode', '4', 1, '客服模式：1企业微信 2电话 4微信二维码 5在线IM', 'content', '客服');
INSERT INTO `sys_config` VALUES (909, 'sys_customer_phone', '', 1, '客服电话（mode=2）', 'content', '客服');
INSERT INTO `sys_config` VALUES (910, 'sys_customer_corp_id', '', 1, '企业微信 corpId（mode=1）', 'content', '客服');
INSERT INTO `sys_config` VALUES (911, 'sys_customer_link', '', 1, '企业微信客服链接（mode=1）', 'content', '客服');
INSERT INTO `sys_config` VALUES (936, 'sys_customer_im_welcome', '您好，在线客服为您服务，请描述您的问题~', 1, '在线IM欢迎语（mode=5）', 'content', '客服');
INSERT INTO `sys_config` VALUES (1006, 'sys_oa_oauth_enabled', '0', 1, '公众号授权登录开关：1=开启 0=关闭', 'sys', '微信');
INSERT INTO `sys_config` VALUES (1012, 'invite_commission_rate_level1', '0.05', 1, '一级邀请佣金比例', 'invite', '邀请配置');
INSERT INTO `sys_config` VALUES (1013, 'invite_commission_rate_level2', '0.02', 1, '二级邀请佣金比例', 'invite', '邀请配置');
INSERT INTO `sys_config` VALUES (1014, 'agreement_user', '<h3>用户服务协议</h3><p>荣e游用户服务协议，待完善。</p>', 1, '用户服务协议 HTML', 'agreement', '协议配置');
INSERT INTO `sys_config` VALUES (1015, 'agreement_privacy', '<h3>隐私政策</h3><p>荣e游隐私政策，待完善。</p>', 1, '隐私政策 HTML', 'agreement', '协议配置');
INSERT INTO `sys_config` VALUES (1016, 'agreement_about', '<h3>关于我们</h3><p>荣e游 — 荣昌文旅服务平台。</p>', 1, '关于我们 HTML', 'agreement', '协议配置');
INSERT INTO `sys_config` VALUES (1017, 'sys_kefu_phone', '023-46789012', 1, '客服电话', 'kefu', '客服配置');
INSERT INTO `sys_config` VALUES (1018, 'sys_kefu_wechat', 'sddas', 1, '客服微信号', 'kefu', '客服配置');
INSERT INTO `sys_config` VALUES (1019, 'sys_kefu_wework_url', 'https://work.weixin.qq.com/kfid/kfc_demo', 1, '企业微信客服链接', 'kefu', '客服配置');
INSERT INTO `sys_config` VALUES (1020, 'sys_kefu_wework_corp_id', 'ww_demo_corp_id', 1, '企业微信 corpId', 'kefu', '客服配置');
INSERT INTO `sys_config` VALUES (1021, 'sys_kefu_type', 'im', 1, '默认客服类型', 'kefu', '客服配置');
INSERT INTO `sys_config` VALUES (1022, 'invite_poster_image', 'https://tu.mianyazuju.com/2026/09/14/aeb24bda-12fa-4972-9719-61e694cea97e.png', 1, '邀请海报图片', 'invite', '邀请配置');
INSERT INTO `sys_config` VALUES (1023, 'invite_copy_text', '我在「荣e游」发现了超棒的荣昌文旅平台，酒店、线路、非遗好物一站式体验！快来加入我们吧~ 邀请码：{code}\n点击链接注册：{link}', 1, '邀请复制文案', 'invite', '邀请配置');
INSERT INTO `sys_config` VALUES (1024, 'invite_rebate_level1', '奖励一级好友消费佣金的5%', 1, '一级返佣说明', 'invite', '邀请配置');
INSERT INTO `sys_config` VALUES (1025, 'invite_rebate_level2', '奖励二级好友消费佣金的2%', 1, '二级返佣说明', 'invite', '邀请配置');
INSERT INTO `sys_config` VALUES (1026, 'invite_share_link', 'http://localhost:5588/', 1, '邀请分享链接', 'invite', '邀请配置');
INSERT INTO `sys_config` VALUES (1027, 'sys_default_avatar', 'https://tu.mianyazuju.com/2026/09/01/eed55c6a-a41e-4a4a-b61a-d29d1ded313a.png', 1, '默认头像', 'sys', '系统配置');
INSERT INTO `sys_config` VALUES (1028, 'map_platform', 'tencent', 1, 'map platform', 'map', 'map config');
INSERT INTO `sys_config` VALUES (1029, 'map_tencent_key', '', 1, 'tencent map key', 'map', 'map config');
INSERT INTO `sys_config` VALUES (1030, 'sys_kefu_wechat_qrcode', 'https://tu.mianyazuju.com/2026/09/15/87beefe9-f411-4275-90d7-220ccb53fbb4.png', 1, '客服微信二维码', 'kefu', '客服配置');
INSERT INTO `sys_config` VALUES (1031, 'sys_kefu_im_url', '', 1, '在线客服 H5 链接', 'kefu', '客服配置');
INSERT INTO `sys_config` VALUES (1032, 'sys_ai_assistant_enabled', '1', 1, '启用 AI 助手入口', 'kefu', '客服配置');
INSERT INTO `sys_config` VALUES (1033, 'sys_withdraw_min', '10', 1, NULL, 'wallet', '撐现配置');
INSERT INTO `sys_config` VALUES (1034, 'sys_withdraw_max', '50000', 1, NULL, 'wallet', '撐现配置');
INSERT INTO `sys_config` VALUES (1035, 'sys_withdraw_rate', '1', 1, '手续费比例', 'wallet', '撐现配置');
INSERT INTO `sys_config` VALUES (1036, 'sys_bus_realtime_enabled', '1', 1, '启用实时公交', 'transport', '公共交通配置');
INSERT INTO `sys_config` VALUES (1037, 'sys_bus_realtime_api_key', '', 1, 'apizero API Key', 'transport', '公共交通配置');
INSERT INTO `sys_config` VALUES (1038, 'sys_platform_commission_rate_hotel', '1', 1, 'hotel commission', 'order', 'order settlement');
INSERT INTO `sys_config` VALUES (1039, 'sys_platform_commission_rate_route', '1', 1, 'route commission', 'order', 'order settlement');
INSERT INTO `sys_config` VALUES (1040, 'sys_route_refund_ratio_before_trip', '1', 1, 'before trip', 'route_order', 'route refund');
INSERT INTO `sys_config` VALUES (1041, 'sys_route_refund_ratio_on_trip_day', '2', 1, 'on trip day', 'route_order', 'route refund');
INSERT INTO `sys_config` VALUES (1042, 'sys_route_refund_ratio_after_trip', '1', 1, 'after trip', 'route_order', 'route refund');
INSERT INTO `sys_config` VALUES (1043, 'sys_route_refund_ratio_noshow', '', 1, 'noshow', 'route_order', 'route refund');
INSERT INTO `sys_config` VALUES (1044, 'sys_route_meet_hold_hour', '10', 1, 'meet hold hour', 'route_order', 'route refund');
INSERT INTO `sys_config` VALUES (1045, 'sys_route_refund_rule_summary', '1. 出行日前取消：可免费取消，全额退款。\n2. 出行日当天取消：扣除实付的2%。\n3. 已核销订单不支持在线退款。', 1, 'route refund summary', 'route_order', 'route refund');
INSERT INTO `sys_config` VALUES (1046, 'ai_platform', 'doubao', 1, 'AI 供应商：local_template（规则兜底）/ doubao（豆包）', 'ai', 'AI 助手');
INSERT INTO `sys_config` VALUES (1047, 'ai_doubao_api_key', '', 1, '豆包 Ark API Key（ai_platform=doubao 时必填）', 'ai', 'AI 助手');
INSERT INTO `sys_config` VALUES (1048, 'ai_doubao_model', '', 1, '豆包模型 ID', 'ai', 'AI 助手');
INSERT INTO `sys_config` VALUES (1049, 'ai_doubao_endpoint', '', 1, '豆包 Chat Completions 接口地址', 'ai', 'AI 助手');
INSERT INTO `sys_config` VALUES (1050, 'lvyou_aliyun_express_appcode', '', 1, '阿里云快递查询 AppCode，用于同步物流轨迹；配额用尽或未配置时展示订单基础物流信息', 'logistics', '物流配置');
INSERT INTO `sys_config` VALUES (1051, 'lvyou_aliyun_express_api_url', '', 1, '阿里云快递查询接口地址，一般无需修改', 'logistics', '物流配置');
INSERT INTO `sys_config` VALUES (1052, 'sys_mp_login_enabled', '1', 1, '是否开启公众号登录：1=开启，0=关闭', 'switch', '开关配置');
INSERT INTO `sys_config` VALUES (1053, 'pay_wx_app_id', '', 1, '微信 App 支付 AppId', 'pay', '微信支付');
INSERT INTO `sys_config` VALUES (1054, 'pay_wx_mch_id', '', 1, '微信支付商户号（与 pay_wechat_mch_id 保持一致）', 'pay', '微信支付');
INSERT INTO `sys_config` VALUES (1055, 'pay_wx_mch_key', '', 1, '微信支付商户密钥（与 pay_wechat_mch_key 保持一致）', 'pay', '微信支付');
INSERT INTO `sys_config` VALUES (1056, 'pay_wx_key_path', '', 1, '微信商户 p12 证书路径（与 sys_wx_key_path 保持一致）', 'pay', '微信支付');
INSERT INTO `sys_config` VALUES (1057, 'sys_user_app_wx_appid', '', 1, '用户端 App 微信 AppId', 'sys', '系统配置');
INSERT INTO `sys_config` VALUES (1058, 'sys_user_app_wx_secret', NULL, 1, '用户端 App 微信 Secret', 'sys', '系统配置');
INSERT INTO `sys_config` VALUES (1059, 'pay_wechat_app_appid', '', 1, '微信 App AppId（支付模块）', 'pay', '微信支付');
INSERT INTO `sys_config` VALUES (1060, 'pay_wechat_h5_appid', '', 1, '微信 H5/公众号 AppId（支付模块）', 'pay', '微信支付');
INSERT INTO `sys_config` VALUES (1061, 'pay_wechat_jsapi_appid', '', 1, '微信小程序 AppId（支付模块）', 'pay', '微信支付');

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字典名称',
  `type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字典类型',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字典码',
  `value` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字典值',
  `order_num` int(0) NULL DEFAULT 0 COMMENT '排序',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `del_flag` int(0) NULL DEFAULT 0 COMMENT '删除标记  -1：已删除  0：正常',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `type`(`type`, `code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '数据字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` VALUES (6, '酒店', 'lvyou_merchant_type', 'hotel', '1.00', 1, '商户门店类型（固定）', 0);
INSERT INTO `sys_dict` VALUES (7, '餐饮', 'lvyou_merchant_type', 'restaurant', '3000.00', 2, '商户门店类型（固定）', 0);
INSERT INTO `sys_dict` VALUES (9, '旅游', 'lvyou_merchant_type', 'travel', '5.00', 3, '商户门店类型（固定）', 0);
INSERT INTO `sys_dict` VALUES (10, '文创', 'lvyou_merchant_type', 'culture', '2000.00', 4, '商户门店类型（固定）', 0);
INSERT INTO `sys_dict` VALUES (11, '全部', 'lvyou_order_filter_hotel', 'all', 'all', 0, '酒店订单Tab筛选', 0);
INSERT INTO `sys_dict` VALUES (12, '待入住', 'lvyou_order_filter_hotel', 'pending_use', 'pending_use', 1, '酒店订单Tab筛选', 0);
INSERT INTO `sys_dict` VALUES (13, '已完成', 'lvyou_order_filter_hotel', 'completed', 'completed', 2, '酒店订单Tab筛选', 0);
INSERT INTO `sys_dict` VALUES (14, '已取消', 'lvyou_order_filter_hotel', 'cancelled', 'cancelled', 3, '酒店订单Tab筛选', 0);
INSERT INTO `sys_dict` VALUES (15, '全部', 'lvyou_order_filter_goods', 'all', 'all', 0, '商品订单Tab筛选', 0);
INSERT INTO `sys_dict` VALUES (16, '待发货', 'lvyou_order_filter_goods', 'pending_use', 'pending_use', 1, '商品订单Tab筛选', 0);
INSERT INTO `sys_dict` VALUES (17, '待收货', 'lvyou_order_filter_goods', 'pending_receive', 'pending_receive', 2, '商品订单Tab筛选', 0);
INSERT INTO `sys_dict` VALUES (18, '已完成', 'lvyou_order_filter_goods', 'completed', 'completed', 3, '商品订单Tab筛选', 0);
INSERT INTO `sys_dict` VALUES (19, '全部', 'lvyou_order_filter_route', 'all', 'all', 0, '线路订单Tab筛选', 0);
INSERT INTO `sys_dict` VALUES (20, '待出行', 'lvyou_order_filter_route', 'pending_use', 'pending_use', 1, '线路订单Tab筛选', 0);
INSERT INTO `sys_dict` VALUES (21, '已完成', 'lvyou_order_filter_route', 'completed', 'completed', 2, '线路订单Tab筛选', 0);
INSERT INTO `sys_dict` VALUES (22, '已取消', 'lvyou_order_filter_route', 'cancelled', 'cancelled', 3, '线路订单Tab筛选', 0);
INSERT INTO `sys_dict` VALUES (23, '待支付', 'lvyou_order_status', 'pending_pay', 'pending_pay', 0, '订单监管状态', 0);
INSERT INTO `sys_dict` VALUES (24, '待确认', 'lvyou_order_status', 'pending_confirm', 'pending_confirm', 1, '订单监管状态', 0);
INSERT INTO `sys_dict` VALUES (25, '待使用/待发货', 'lvyou_order_status', 'pending_use', 'pending_use', 2, '订单监管状态', 0);
INSERT INTO `sys_dict` VALUES (26, '待收货', 'lvyou_order_status', 'pending_receive', 'pending_receive', 3, '订单监管状态', 0);
INSERT INTO `sys_dict` VALUES (27, '已完成', 'lvyou_order_status', 'completed', 'completed', 4, '订单监管状态', 0);
INSERT INTO `sys_dict` VALUES (28, '已取消', 'lvyou_order_status', 'cancelled', 'cancelled', 5, '订单监管状态', 0);
INSERT INTO `sys_dict` VALUES (29, '退款中', 'lvyou_order_status', 'refunding', 'refunding', 6, '订单监管状态', 0);
INSERT INTO `sys_dict` VALUES (30, '全部', 'lvyou_reservation_filter', 'all', 'all', 0, '餐饮预约Tab筛选', 0);
INSERT INTO `sys_dict` VALUES (31, '待到店', 'lvyou_reservation_filter', 'confirmed', 'confirmed', 1, '餐饮预约Tab筛选', 0);
INSERT INTO `sys_dict` VALUES (32, '已完成', 'lvyou_reservation_filter', 'completed', 'completed', 2, '餐饮预约Tab筛选', 0);
INSERT INTO `sys_dict` VALUES (33, '已取消', 'lvyou_reservation_filter', 'cancelled', 'cancelled', 3, '餐饮预约Tab筛选', 0);
INSERT INTO `sys_dict` VALUES (34, 'Banner图', 'lvyou_banner_classify', '1', '1', 0, '轮播位置分类', 0);
INSERT INTO `sys_dict` VALUES (35, '金刚区', 'lvyou_banner_classify', '2', '2', 1, '轮播位置分类', 0);
INSERT INTO `sys_dict` VALUES (36, '全场通用', 'lvyou_coupon_scope', 'all', 'all', 0, '优惠券适用范围', 0);
INSERT INTO `sys_dict` VALUES (37, '酒店预订', 'lvyou_coupon_scope', 'hotel', 'hotel', 1, '优惠券适用范围', 0);
INSERT INTO `sys_dict` VALUES (38, '旅游线路', 'lvyou_coupon_scope', 'route', 'route', 2, '优惠券适用范围', 0);
INSERT INTO `sys_dict` VALUES (39, '非遗好物', 'lvyou_coupon_scope', 'goods', 'goods', 3, '优惠券适用范围', 0);
INSERT INTO `sys_dict` VALUES (40, '已退款', 'lvyou_order_filter_hotel', 'refunded', 'refunded', 4, '酒店订单Tab筛选', 0);

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '用户名',
  `operation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '用户操作',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '请求方法',
  `params` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '请求参数',
  `time` bigint(0) NOT NULL COMMENT '执行时长(毫秒)',
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'IP地址',
  `create_date` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 52 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '系统日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_log
-- ----------------------------
INSERT INTO `sys_log` VALUES (1, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":8,\"paramKey\":\"sys_server_name\",\"paramValue\":\"西安文旅\",\"status\":\"1\",\"remark\":\"产品名称（用户端与管理端展示，亦用于琭信/邮件签名）\",\"paramType\":\"sys\",\"paramTypeDesc\":\"系统配置\"}]', 47, '0:0:0:0:0:0:0:1', '2026-09-10 10:52:27');
INSERT INTO `sys_log` VALUES (2, 'admin', '刷新缓存', 'com.sqx.system.controller.SysConfigController.refresh()', '[]', 30, '0:0:0:0:0:0:0:1', '2026-09-10 10:52:27');
INSERT INTO `sys_log` VALUES (3, 'admin', '修改菜单', 'com.sqx.system.controller.SysMenuController.update()', '[{\"menuId\":900030,\"parentId\":900103,\"name\":\"首页配置\",\"url\":\"homeModule\",\"perms\":\"business-home-module\",\"type\":1,\"icon\":\"menu\",\"orderNum\":5}]', 10, '0:0:0:0:0:0:0:1', '2026-09-10 11:24:03');
INSERT INTO `sys_log` VALUES (4, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1026,\"paramKey\":\"invite_share_link\",\"paramValue\":\"http://localhost:5588/\",\"status\":\"1\",\"remark\":\"邀请分享链接\",\"paramType\":\"invite\",\"paramTypeDesc\":\"邀请配置\"}]', 30, '0:0:0:0:0:0:0:1', '2026-09-14 13:54:19');
INSERT INTO `sys_log` VALUES (5, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1022,\"paramKey\":\"invite_poster_image\",\"paramValue\":\"https://tu.mianyazuju.com/2026/09/14/aeb24bda-12fa-4972-9719-61e694cea97e.png\",\"status\":\"1\",\"remark\":\"邀请海报图片\",\"paramType\":\"invite\",\"paramTypeDesc\":\"邀请配置\"}]', 31, '0:0:0:0:0:0:0:1', '2026-09-14 13:54:19');
INSERT INTO `sys_log` VALUES (6, 'admin', '刷新缓存', 'com.sqx.system.controller.SysConfigController.refresh()', '[]', 33, '0:0:0:0:0:0:0:1', '2026-09-14 13:54:19');
INSERT INTO `sys_log` VALUES (7, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1018,\"paramKey\":\"sys_kefu_wechat\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"客服微信号\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 15, '0:0:0:0:0:0:0:1', '2026-09-14 15:42:15');
INSERT INTO `sys_log` VALUES (8, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1017,\"paramKey\":\"sys_kefu_phone\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"客服电话\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 17, '0:0:0:0:0:0:0:1', '2026-09-14 15:42:15');
INSERT INTO `sys_log` VALUES (9, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1019,\"paramKey\":\"sys_kefu_wework_url\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"企业微信客服链接\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 15, '0:0:0:0:0:0:0:1', '2026-09-14 15:42:15');
INSERT INTO `sys_log` VALUES (10, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1021,\"paramKey\":\"sys_kefu_type\",\"paramValue\":\"im\",\"status\":\"1\",\"remark\":\"默认客服类型\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 19, '0:0:0:0:0:0:0:1', '2026-09-14 15:42:15');
INSERT INTO `sys_log` VALUES (11, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1020,\"paramKey\":\"sys_kefu_wework_corp_id\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"企业微信 corpId\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 15, '0:0:0:0:0:0:0:1', '2026-09-14 15:42:15');
INSERT INTO `sys_log` VALUES (12, 'admin', '刷新缓存', 'com.sqx.system.controller.SysConfigController.refresh()', '[]', 42, '0:0:0:0:0:0:0:1', '2026-09-14 15:42:15');
INSERT INTO `sys_log` VALUES (13, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1020,\"paramKey\":\"sys_kefu_wework_corp_id\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"企业微信 corpId\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 46, '0:0:0:0:0:0:0:1', '2026-09-15 09:35:27');
INSERT INTO `sys_log` VALUES (14, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1019,\"paramKey\":\"sys_kefu_wework_url\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"企业微信客服链接\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 46, '0:0:0:0:0:0:0:1', '2026-09-15 09:35:27');
INSERT INTO `sys_log` VALUES (15, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1017,\"paramKey\":\"sys_kefu_phone\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"客服电话\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 46, '0:0:0:0:0:0:0:1', '2026-09-15 09:35:27');
INSERT INTO `sys_log` VALUES (16, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1030,\"paramKey\":\"sys_kefu_wechat_qrcode\",\"paramValue\":\"https://tu.mianyazuju.com/2026/09/15/87beefe9-f411-4275-90d7-220ccb53fbb4.png\",\"status\":\"1\",\"remark\":\"客服微信二维码\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 51, '0:0:0:0:0:0:0:1', '2026-09-15 09:35:27');
INSERT INTO `sys_log` VALUES (17, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1021,\"paramKey\":\"sys_kefu_type\",\"paramValue\":\"wechat\",\"status\":\"1\",\"remark\":\"默认客服类型\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 51, '0:0:0:0:0:0:0:1', '2026-09-15 09:35:27');
INSERT INTO `sys_log` VALUES (18, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1018,\"paramKey\":\"sys_kefu_wechat\",\"paramValue\":\"sddas\",\"status\":\"1\",\"remark\":\"客服微信号\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 51, '0:0:0:0:0:0:0:1', '2026-09-15 09:35:27');
INSERT INTO `sys_log` VALUES (19, 'admin', '刷新缓存', 'com.sqx.system.controller.SysConfigController.refresh()', '[]', 31, '0:0:0:0:0:0:0:1', '2026-09-15 09:35:27');
INSERT INTO `sys_log` VALUES (20, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1019,\"paramKey\":\"sys_kefu_wework_url\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"企业微信客服链接\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 61, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:27');
INSERT INTO `sys_log` VALUES (21, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1020,\"paramKey\":\"sys_kefu_wework_corp_id\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"企业微信 corpId\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 61, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:27');
INSERT INTO `sys_log` VALUES (22, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1017,\"paramKey\":\"sys_kefu_phone\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"客服电话\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 61, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:27');
INSERT INTO `sys_log` VALUES (23, 'admin', '刷新缓存', 'com.sqx.system.controller.SysConfigController.refresh()', '[]', 18, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:27');
INSERT INTO `sys_log` VALUES (24, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1018,\"paramKey\":\"sys_kefu_wechat\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"客服微信号\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 5, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:43');
INSERT INTO `sys_log` VALUES (25, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1030,\"paramKey\":\"sys_kefu_wechat_qrcode\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"客服微信二维码\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 5, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:43');
INSERT INTO `sys_log` VALUES (26, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1020,\"paramKey\":\"sys_kefu_wework_corp_id\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"企业微信 corpId\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 4, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:43');
INSERT INTO `sys_log` VALUES (27, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1019,\"paramKey\":\"sys_kefu_wework_url\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"企业微信客服链接\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 5, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:43');
INSERT INTO `sys_log` VALUES (28, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1021,\"paramKey\":\"sys_kefu_type\",\"paramValue\":\"phone\",\"status\":\"1\",\"remark\":\"默认客服类型\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 11, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:43');
INSERT INTO `sys_log` VALUES (29, 'admin', '刷新缓存', 'com.sqx.system.controller.SysConfigController.refresh()', '[]', 12, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:43');
INSERT INTO `sys_log` VALUES (30, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1018,\"paramKey\":\"sys_kefu_wechat\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"客服微信号\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 4, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:52');
INSERT INTO `sys_log` VALUES (31, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1019,\"paramKey\":\"sys_kefu_wework_url\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"企业微信客服链接\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 4, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:52');
INSERT INTO `sys_log` VALUES (32, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1020,\"paramKey\":\"sys_kefu_wework_corp_id\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"企业微信 corpId\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 4, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:52');
INSERT INTO `sys_log` VALUES (33, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1030,\"paramKey\":\"sys_kefu_wechat_qrcode\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"客服微信二维码\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 4, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:52');
INSERT INTO `sys_log` VALUES (34, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1017,\"paramKey\":\"sys_kefu_phone\",\"paramValue\":\"\",\"status\":\"1\",\"remark\":\"客服电话\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 4, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:52');
INSERT INTO `sys_log` VALUES (35, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1021,\"paramKey\":\"sys_kefu_type\",\"paramValue\":\"im\",\"status\":\"1\",\"remark\":\"默认客服类型\",\"paramType\":\"kefu\",\"paramTypeDesc\":\"客服配置\"}]', 13, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:52');
INSERT INTO `sys_log` VALUES (36, 'admin', '刷新缓存', 'com.sqx.system.controller.SysConfigController.refresh()', '[]', 9, '0:0:0:0:0:0:0:1', '2026-09-15 09:50:52');
INSERT INTO `sys_log` VALUES (37, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1029,\"paramKey\":\"map_tencent_key\",\"paramValue\":\"UPPBZ-ETDW3-2CH3D-OBTN6-FEKAZ-6MFD6\",\"status\":\"1\",\"remark\":\"tencent map key\",\"paramType\":\"map\",\"paramTypeDesc\":\"map config\"}]', 50, '0:0:0:0:0:0:0:1', '2026-09-16 09:50:36');
INSERT INTO `sys_log` VALUES (38, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1037,\"paramKey\":\"sys_bus_realtime_api_key\",\"paramValue\":\"sk_test_4e343e3d2632bf4521db9c86799de6677d3ce01b51db7868\",\"status\":\"1\",\"remark\":\"apizero API Key\",\"paramType\":\"transport\",\"paramTypeDesc\":\"公共交通配置\"}]', 26, '0:0:0:0:0:0:0:1', '2026-09-18 16:18:13');
INSERT INTO `sys_log` VALUES (39, 'admin', '刷新缓存', 'com.sqx.system.controller.SysConfigController.refresh()', '[]', 33, '0:0:0:0:0:0:0:1', '2026-09-18 16:18:13');
INSERT INTO `sys_log` VALUES (40, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1041,\"paramKey\":\"sys_route_refund_ratio_on_trip_day\",\"paramValue\":\"2\",\"status\":\"1\",\"remark\":\"on trip day\",\"paramType\":\"route_order\",\"paramTypeDesc\":\"route refund\"}]', 97, '0:0:0:0:0:0:0:1', '2026-09-23 13:27:36');
INSERT INTO `sys_log` VALUES (41, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1042,\"paramKey\":\"sys_route_refund_ratio_after_trip\",\"paramValue\":\"5\",\"status\":\"1\",\"remark\":\"after trip\",\"paramType\":\"route_order\",\"paramTypeDesc\":\"route refund\"}]', 97, '0:0:0:0:0:0:0:1', '2026-09-23 13:27:36');
INSERT INTO `sys_log` VALUES (42, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1045,\"paramKey\":\"sys_route_refund_rule_summary\",\"paramValue\":\"1. 出行日前取消：可免费取消，全额退款。\\n2. 出行日当天取消：退还实付的2%。\\n3. 已核销订单不支持在线退款。\",\"status\":\"1\",\"remark\":\"route refund summary\",\"paramType\":\"route_order\",\"paramTypeDesc\":\"route refund\"}]', 97, '0:0:0:0:0:0:0:1', '2026-09-23 13:27:36');
INSERT INTO `sys_log` VALUES (43, 'admin', '刷新缓存', 'com.sqx.system.controller.SysConfigController.refresh()', '[]', 40, '0:0:0:0:0:0:0:1', '2026-09-23 13:27:36');
INSERT INTO `sys_log` VALUES (44, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1042,\"paramKey\":\"sys_route_refund_ratio_after_trip\",\"paramValue\":\"1\",\"status\":\"1\",\"remark\":\"after trip\",\"paramType\":\"route_order\",\"paramTypeDesc\":\"route refund\"}]', 28, '0:0:0:0:0:0:0:1', '2026-09-23 14:25:42');
INSERT INTO `sys_log` VALUES (45, 'admin', '刷新缓存', 'com.sqx.system.controller.SysConfigController.refresh()', '[]', 35, '0:0:0:0:0:0:0:1', '2026-09-23 14:25:42');
INSERT INTO `sys_log` VALUES (46, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1039,\"paramKey\":\"sys_platform_commission_rate_route\",\"paramValue\":\"1\",\"status\":\"1\",\"remark\":\"route commission\",\"paramType\":\"order\",\"paramTypeDesc\":\"order settlement\"}]', 12, '0:0:0:0:0:0:0:1', '2026-09-23 14:28:14');
INSERT INTO `sys_log` VALUES (47, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1038,\"paramKey\":\"sys_platform_commission_rate_hotel\",\"paramValue\":\"1\",\"status\":\"1\",\"remark\":\"hotel commission\",\"paramType\":\"order\",\"paramTypeDesc\":\"order settlement\"}]', 12, '0:0:0:0:0:0:0:1', '2026-09-23 14:28:14');
INSERT INTO `sys_log` VALUES (48, 'admin', '刷新缓存', 'com.sqx.system.controller.SysConfigController.refresh()', '[]', 22, '0:0:0:0:0:0:0:1', '2026-09-23 14:28:15');
INSERT INTO `sys_log` VALUES (49, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1040,\"paramKey\":\"sys_route_refund_ratio_before_trip\",\"paramValue\":\"1\",\"status\":\"1\",\"remark\":\"before trip\",\"paramType\":\"route_order\",\"paramTypeDesc\":\"route refund\"}]', 32, '0:0:0:0:0:0:0:1', '2026-09-23 15:30:53');
INSERT INTO `sys_log` VALUES (50, 'admin', '修改配置', 'com.sqx.system.controller.SysConfigController.update()', '[{\"id\":1045,\"paramKey\":\"sys_route_refund_rule_summary\",\"paramValue\":\"1. 出行日前取消：可免费取消，全额退款。\\n2. 出行日当天取消：扣除实付的2%。\\n3. 已核销订单不支持在线退款。\",\"status\":\"1\",\"remark\":\"route refund summary\",\"paramType\":\"route_order\",\"paramTypeDesc\":\"route refund\"}]', 33, '0:0:0:0:0:0:0:1', '2026-09-23 15:30:53');
INSERT INTO `sys_log` VALUES (51, 'admin', '刷新缓存', 'com.sqx.system.controller.SysConfigController.refresh()', '[]', 34, '0:0:0:0:0:0:0:1', '2026-09-23 15:30:53');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(0) NULL DEFAULT NULL COMMENT '父菜单ID，一级菜单为0',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '菜单名称',
  `url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '菜单URL',
  `perms` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '授权(多个用逗号分隔，如：user:list,user:create)',
  `type` int(0) NULL DEFAULT NULL COMMENT '类型   0：目录   1：菜单   2：按钮',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '菜单图标',
  `order_num` int(0) NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 901374 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '菜单管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '系统管理', NULL, NULL, 0, 'icon-system', 12);
INSERT INTO `sys_menu` VALUES (2, 1, '管理员管理', 'sys/user', 'sys:user:list', 1, 'icon-geren', 1);
INSERT INTO `sys_menu` VALUES (3, 2, '查看', NULL, 'sys:user:list', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (4, 2, '新增', NULL, 'sys:user:save', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (5, 2, '修改', NULL, 'sys:user:update', 2, NULL, 3);
INSERT INTO `sys_menu` VALUES (6, 2, '删除', NULL, 'sys:user:delete', 2, NULL, 4);
INSERT INTO `sys_menu` VALUES (7, 2, '详情', NULL, 'sys:user:info', 2, NULL, 5);
INSERT INTO `sys_menu` VALUES (8, 1, '角色管理', 'sys/role', 'sys:role:list', 1, 'icon-role', 2);
INSERT INTO `sys_menu` VALUES (9, 8, '查看', NULL, 'sys:role:list', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (10, 8, '新增', NULL, 'sys:role:save', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (11, 8, '修改', NULL, 'sys:role:update', 2, NULL, 3);
INSERT INTO `sys_menu` VALUES (12, 8, '删除', NULL, 'sys:role:delete', 2, NULL, 4);
INSERT INTO `sys_menu` VALUES (13, 8, '授权', NULL, 'sys:role:select', 2, NULL, 5);
INSERT INTO `sys_menu` VALUES (14, 1, '菜单管理', 'sys/menu', 'sys:menu:list', 1, 'icon-menu', 3);
INSERT INTO `sys_menu` VALUES (15, 14, '查看', NULL, 'sys:menu:list', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (16, 14, '新增', NULL, 'sys:menu:save', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (17, 14, '修改', NULL, 'sys:menu:update', 2, NULL, 3);
INSERT INTO `sys_menu` VALUES (18, 14, '删除', NULL, 'sys:menu:delete', 2, NULL, 4);
INSERT INTO `sys_menu` VALUES (19, 1, '字典管理', 'sys/dict', 'sys:dict:list', 1, 'icon-fenleilist', 4);
INSERT INTO `sys_menu` VALUES (20, 19, '查看', NULL, 'sys:dict:list', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (21, 19, '新增', NULL, 'sys:dict:save', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (22, 19, '修改', NULL, 'sys:dict:update', 2, NULL, 3);
INSERT INTO `sys_menu` VALUES (23, 19, '删除', NULL, 'sys:dict:delete', 2, NULL, 4);
INSERT INTO `sys_menu` VALUES (24, 1, '系统配置', 'sys/config', 'sys:config:list', 1, 'icon-peizhi', 5);
INSERT INTO `sys_menu` VALUES (25, 24, '查看', NULL, 'sys:config:list', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (26, 24, '新增', NULL, 'sys:config:save', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (27, 24, '修改', NULL, 'sys:config:update', 2, NULL, 3);
INSERT INTO `sys_menu` VALUES (28, 24, '删除', NULL, 'sys:config:delete', 2, NULL, 4);
INSERT INTO `sys_menu` VALUES (29, 1, '操作日志', 'sys/log', 'sys:log:list', 1, 'icon-log', 6);
INSERT INTO `sys_menu` VALUES (30, 29, '查看', NULL, 'sys:log:list', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (31, 1, '权限管理', 'permission', 'sys:menu:list', 1, 'icon-shezhi', 7);
INSERT INTO `sys_menu` VALUES (237, 1, '文件存储', '/system/file-storage', 'sys:config:list', 2, NULL, 8);
INSERT INTO `sys_menu` VALUES (900001, 0, '业务统计', '/dashboard', 'admin:stats:view', 1, 'shuju', 1);
INSERT INTO `sys_menu` VALUES (900012, 900103, '消息管理', 'messageList', 'business-messages', 1, 'xiaoxi', 2);
INSERT INTO `sys_menu` VALUES (900013, 1, '短信模板', 'smsTemplate', 'system-sms-templates', 1, 'duanxin', 7);
INSERT INTO `sys_menu` VALUES (900014, 900100, '商户审核', 'merchantApply', 'business-merchant-audit', 1, 'admin', 1);
INSERT INTO `sys_menu` VALUES (900015, 900101, '订单监管', 'lvyouOrder', 'business-order', 1, 'order', 1);
INSERT INTO `sys_menu` VALUES (900017, 900102, '酒店管理', 'hotelList', 'business-hotel', 1, 'jd', 2);
INSERT INTO `sys_menu` VALUES (900018, 900102, '线路管理', 'routeList', 'business-route', 1, 'daohang', 3);
INSERT INTO `sys_menu` VALUES (900019, 900102, '活动管理', 'activityList', 'business-activity', 1, 'renwu', 4);
INSERT INTO `sys_menu` VALUES (900020, 900102, '景区管理', 'scenicList', 'business-scenic', 1, 'xiangqufill', 5);
INSERT INTO `sys_menu` VALUES (900021, 900103, '公告管理', 'noticeList', 'business-notice', 1, 'tixing', 4);
INSERT INTO `sys_menu` VALUES (900022, 900101, '优惠券模板', 'couponTemplate', 'business-coupon', 1, 'jifen', 3);
INSERT INTO `sys_menu` VALUES (900023, 0, '提现审核', 'withdrawAudit', 'business-withdraw', 1, 'caiwu', 13);
INSERT INTO `sys_menu` VALUES (900024, 0, '非遗内容', 'heritageManage', 'business-heritage', 1, 'editor', 7);
INSERT INTO `sys_menu` VALUES (900026, 900101, '支付监管', 'paymentManage', 'business-payment', 1, 'pingtai', 2);
INSERT INTO `sys_menu` VALUES (900027, 0, '平台配置', 'platformConfig', 'business-platform-config', 1, 'config', 11);
INSERT INTO `sys_menu` VALUES (900028, 0, '餐饮预约', 'canteenList', 'business-canteen', 1, 'shangpin', 9);
INSERT INTO `sys_menu` VALUES (900029, 0, '邀请分销', 'inviteManage', 'business-invite', 1, 'yonghul', 10);
INSERT INTO `sys_menu` VALUES (900030, 900103, '首页配置', 'homeModule', 'business-home-module', 1, 'menu', 5);
INSERT INTO `sys_menu` VALUES (900031, 900100, '商户管理', 'merchantManage', 'business-merchant-manage', 1, 'ordercenter', 2);
INSERT INTO `sys_menu` VALUES (900032, 0, 'C端用户', 'userList', 'business-users', 1, 'yonghu', 2);
INSERT INTO `sys_menu` VALUES (900034, 0, '注销审核', 'cancelAccountList', 'business-cancel-account', 1, 'yonghu', 3);
INSERT INTO `sys_menu` VALUES (900035, 0, '在线聊天', 'kefuIm', 'business-kefu-im', 1, 'xiaoxi', 12);
INSERT INTO `sys_menu` VALUES (900036, 900100, '入驻类型', 'merchantStoreType', 'business-merchant-store-type', 1, 'shop', 4);
INSERT INTO `sys_menu` VALUES (900100, 0, '商户中心', '', '', 0, 'admin', 3);
INSERT INTO `sys_menu` VALUES (900101, 0, '交易监管', '', '', 0, 'order', 4);
INSERT INTO `sys_menu` VALUES (900102, 0, '文旅资源', '', '', 0, 'xiangqufill', 5);
INSERT INTO `sys_menu` VALUES (900103, 0, '运营触达', '', '', 0, 'xiaoxi', 6);
INSERT INTO `sys_menu` VALUES (900114, 900014, '查看', NULL, 'lvyou:merchant:apply:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (900115, 900014, '审核', NULL, 'lvyou:merchant:apply:audit', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (900215, 900015, '列表', NULL, 'lvyou:order:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (900216, 900015, '详情', NULL, 'lvyou:order:info', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (900217, 900015, '处理', NULL, 'lvyou:order:action', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (900316, 900102, '目的地查询', NULL, 'lvyou:destination:page', 2, NULL, 99);
INSERT INTO `sys_menu` VALUES (900417, 900017, '列表', NULL, 'lvyou:hotel:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (900418, 900017, '新增', NULL, 'lvyou:hotel:save', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (900419, 900017, '修改', NULL, 'lvyou:hotel:update', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (900420, 900017, '删除', NULL, 'lvyou:hotel:delete', 2, NULL, 3);
INSERT INTO `sys_menu` VALUES (900518, 900018, '列表', NULL, 'lvyou:route:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (900519, 900018, '新增', NULL, 'lvyou:route:save', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (900520, 900018, '修改', NULL, 'lvyou:route:update', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (900521, 900018, '删除', NULL, 'lvyou:route:delete', 2, NULL, 3);
INSERT INTO `sys_menu` VALUES (900522, 900018, '上下架', NULL, 'lvyou:route:updateState', 2, NULL, 4);
INSERT INTO `sys_menu` VALUES (900619, 900019, '列表', NULL, 'lvyou:activity:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (900620, 900019, '新增', NULL, 'lvyou:activity:save', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (900621, 900019, '修改', NULL, 'lvyou:activity:update', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (900622, 900019, '删除', NULL, 'lvyou:activity:delete', 2, NULL, 3);
INSERT INTO `sys_menu` VALUES (900623, 900019, '上下架', NULL, 'lvyou:activity:updateState', 2, NULL, 4);
INSERT INTO `sys_menu` VALUES (900720, 900020, '列表', NULL, 'lvyou:scenic:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (900721, 900020, '新增', NULL, 'lvyou:scenic:save', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (900722, 900020, '修改', NULL, 'lvyou:scenic:update', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (900723, 900020, '删除', NULL, 'lvyou:scenic:delete', 2, NULL, 3);
INSERT INTO `sys_menu` VALUES (900724, 900020, '上下架', NULL, 'lvyou:scenic:updateState', 2, NULL, 4);
INSERT INTO `sys_menu` VALUES (900821, 900021, '列表', NULL, 'lvyou:notice:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (900822, 900021, '新增', NULL, 'lvyou:notice:save', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (900823, 900021, '修改', NULL, 'lvyou:notice:update', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (900824, 900021, '删除', NULL, 'lvyou:notice:delete', 2, NULL, 3);
INSERT INTO `sys_menu` VALUES (900825, 900021, '上下架', NULL, 'lvyou:notice:updateState', 2, NULL, 4);
INSERT INTO `sys_menu` VALUES (900922, 900022, '列表', NULL, 'lvyou:coupon:template:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (900923, 900022, '新增', NULL, 'lvyou:coupon:template:save', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (900924, 900022, '修改', NULL, 'lvyou:coupon:template:update', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (900925, 900022, '删除', NULL, 'lvyou:coupon:template:delete', 2, NULL, 3);
INSERT INTO `sys_menu` VALUES (901023, 900023, '列表', NULL, 'lvyou:withdraw:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (901024, 900023, '审核', NULL, 'lvyou:withdraw:audit', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (901124, 900024, '列表', NULL, 'lvyou:heritage:shop:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (901125, 900024, '新增', NULL, 'lvyou:heritage:shop:save', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (901126, 900024, '修改', NULL, 'lvyou:heritage:shop:update', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (901127, 900024, '删除', NULL, 'lvyou:heritage:shop:delete', 2, NULL, 3);
INSERT INTO `sys_menu` VALUES (901128, 900024, '上下架', NULL, 'lvyou:heritage:shop:updateState', 2, NULL, 4);
INSERT INTO `sys_menu` VALUES (901129, 900024, '工艺列表', NULL, 'lvyou:heritage:craft:page', 2, NULL, 5);
INSERT INTO `sys_menu` VALUES (901130, 900024, '工艺新增', NULL, 'lvyou:heritage:craft:save', 2, NULL, 6);
INSERT INTO `sys_menu` VALUES (901131, 900024, '工艺修改', NULL, 'lvyou:heritage:craft:update', 2, NULL, 7);
INSERT INTO `sys_menu` VALUES (901132, 900024, '工艺删除', NULL, 'lvyou:heritage:craft:delete', 2, NULL, 8);
INSERT INTO `sys_menu` VALUES (901133, 900024, '工艺上下架', NULL, 'lvyou:heritage:craft:updateState', 2, NULL, 9);
INSERT INTO `sys_menu` VALUES (901134, 900024, '商品列表', NULL, 'lvyou:goods:page', 2, NULL, 10);
INSERT INTO `sys_menu` VALUES (901135, 900024, '商品新增', NULL, 'lvyou:goods:save', 2, NULL, 11);
INSERT INTO `sys_menu` VALUES (901136, 900024, '商品修改', NULL, 'lvyou:goods:update', 2, NULL, 12);
INSERT INTO `sys_menu` VALUES (901137, 900024, '商品删除', NULL, 'lvyou:goods:delete', 2, NULL, 13);
INSERT INTO `sys_menu` VALUES (901326, 900026, '流水列表', NULL, 'lvyou:payment:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (901327, 900026, '流水详情', NULL, 'lvyou:payment:info', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (901328, 900026, '渠道配置', NULL, 'lvyou:payment:config:page', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (901329, 900026, '配置修改', NULL, 'lvyou:payment:config:update', 2, NULL, 3);
INSERT INTO `sys_menu` VALUES (901330, 900027, '配置查看', NULL, 'sys:config:list', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (901331, 900027, '配置修改', NULL, 'sys:config:update', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (901332, 900027, '刷新缓存', NULL, 'sys:config:update', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (901333, 900028, '列表', NULL, 'lvyou:canteen:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (901334, 900028, '详情', NULL, 'lvyou:canteen:info', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (901335, 900028, '处理', NULL, 'lvyou:canteen:action', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (901336, 900029, '邀请关系', NULL, 'lvyou:invite:relation:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (901337, 900029, '佣金明细', NULL, 'lvyou:invite:commission:page', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (901338, 900031, '列表', NULL, 'lvyou:merchant:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (901339, 900031, '详情', NULL, 'lvyou:merchant:info', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (901340, 900030, '模块列表', NULL, 'lvyou:homeModule:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (901341, 900030, '新增模块', NULL, 'lvyou:homeModule:save', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (901342, 900030, '编辑模块', NULL, 'lvyou:homeModule:update', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (901343, 900030, '删除模块', NULL, 'lvyou:homeModule:delete', 2, NULL, 3);
INSERT INTO `sys_menu` VALUES (901350, 900031, '调整', NULL, 'lvyou:merchant:adjust', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (901351, 14, '下拉选择', NULL, 'sys:menu:select', 2, NULL, 5);
INSERT INTO `sys_menu` VALUES (901352, 14, '详情', NULL, 'sys:menu:info', 2, NULL, 6);
INSERT INTO `sys_menu` VALUES (901353, 900032, '列表', NULL, 'base:user:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (901354, 900032, '详情', NULL, 'base:user:info', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (901355, 900032, '修改', NULL, 'base:user:update', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (901356, 900032, '删除', NULL, 'base:user:delete', 2, NULL, 3);
INSERT INTO `sys_menu` VALUES (901360, 900034, '列表', NULL, 'base:cancelAccount:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (901361, 900034, '审核', NULL, 'base:cancelAccount:audit', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (901370, 900035, '会话列表', NULL, 'lvyou:kefuIm:session:page', 2, NULL, 0);
INSERT INTO `sys_menu` VALUES (901371, 900035, '查看消息', NULL, 'lvyou:kefuIm:message:page', 2, NULL, 1);
INSERT INTO `sys_menu` VALUES (901372, 900035, '回复消息', NULL, 'lvyou:kefuIm:message:reply', 2, NULL, 2);
INSERT INTO `sys_menu` VALUES (901373, 900035, '关闭会话', NULL, 'lvyou:kefuIm:session:close', 2, NULL, 3);

-- ----------------------------
-- Table structure for sys_oss
-- ----------------------------
DROP TABLE IF EXISTS `sys_oss`;
CREATE TABLE `sys_oss`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'URL地址',
  `create_date` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '文件上传' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oss
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '角色名称',
  `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `create_user_id` bigint(0) NULL DEFAULT NULL COMMENT '创建者ID',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '角色' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'super', 10, '2021-06-04 17:38:28');
INSERT INTO `sys_role` VALUES (2, '运营只读', '仅商城订单查看，无发货/商品写权限', 1, '2026-07-18 05:44:10');
INSERT INTO `sys_role` VALUES (4, '普通用户', '普通用户', 2, '2021-09-10 15:33:47');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(0) NULL DEFAULT NULL COMMENT '角色ID',
  `menu_id` bigint(0) NULL DEFAULT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15168 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '角色与菜单对应关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (13253, 4, 2);
INSERT INTO `sys_role_menu` VALUES (13254, 4, 15);
INSERT INTO `sys_role_menu` VALUES (13255, 4, 16);
INSERT INTO `sys_role_menu` VALUES (13256, 4, 17);
INSERT INTO `sys_role_menu` VALUES (13257, 4, 18);
INSERT INTO `sys_role_menu` VALUES (13258, 4, 3);
INSERT INTO `sys_role_menu` VALUES (13259, 4, 19);
INSERT INTO `sys_role_menu` VALUES (13260, 4, 20);
INSERT INTO `sys_role_menu` VALUES (13261, 4, 21);
INSERT INTO `sys_role_menu` VALUES (13262, 4, 22);
INSERT INTO `sys_role_menu` VALUES (13263, 4, 4);
INSERT INTO `sys_role_menu` VALUES (13264, 4, 23);
INSERT INTO `sys_role_menu` VALUES (13265, 4, 24);
INSERT INTO `sys_role_menu` VALUES (13266, 4, 25);
INSERT INTO `sys_role_menu` VALUES (13267, 4, 26);
INSERT INTO `sys_role_menu` VALUES (13268, 4, NULL);
INSERT INTO `sys_role_menu` VALUES (14594, 1, 1);
INSERT INTO `sys_role_menu` VALUES (14595, 1, 2);
INSERT INTO `sys_role_menu` VALUES (14596, 1, 3);
INSERT INTO `sys_role_menu` VALUES (14597, 1, 4);
INSERT INTO `sys_role_menu` VALUES (14598, 1, 5);
INSERT INTO `sys_role_menu` VALUES (14599, 1, 6);
INSERT INTO `sys_role_menu` VALUES (14600, 1, 7);
INSERT INTO `sys_role_menu` VALUES (14601, 1, 8);
INSERT INTO `sys_role_menu` VALUES (14602, 1, 9);
INSERT INTO `sys_role_menu` VALUES (14603, 1, 10);
INSERT INTO `sys_role_menu` VALUES (14604, 1, 11);
INSERT INTO `sys_role_menu` VALUES (14605, 1, 12);
INSERT INTO `sys_role_menu` VALUES (14606, 1, 13);
INSERT INTO `sys_role_menu` VALUES (14607, 1, 14);
INSERT INTO `sys_role_menu` VALUES (14608, 1, 15);
INSERT INTO `sys_role_menu` VALUES (14609, 1, 16);
INSERT INTO `sys_role_menu` VALUES (14610, 1, 17);
INSERT INTO `sys_role_menu` VALUES (14611, 1, 18);
INSERT INTO `sys_role_menu` VALUES (14612, 1, 19);
INSERT INTO `sys_role_menu` VALUES (14613, 1, 20);
INSERT INTO `sys_role_menu` VALUES (14614, 1, 21);
INSERT INTO `sys_role_menu` VALUES (14615, 1, 22);
INSERT INTO `sys_role_menu` VALUES (14616, 1, 23);
INSERT INTO `sys_role_menu` VALUES (14617, 1, 24);
INSERT INTO `sys_role_menu` VALUES (14618, 1, 25);
INSERT INTO `sys_role_menu` VALUES (14619, 1, 26);
INSERT INTO `sys_role_menu` VALUES (14620, 1, 27);
INSERT INTO `sys_role_menu` VALUES (14621, 1, 28);
INSERT INTO `sys_role_menu` VALUES (14622, 1, 29);
INSERT INTO `sys_role_menu` VALUES (14623, 1, 30);
INSERT INTO `sys_role_menu` VALUES (14624, 1, 31);
INSERT INTO `sys_role_menu` VALUES (15017, 1, 900030);
INSERT INTO `sys_role_menu` VALUES (15018, 1, 901340);
INSERT INTO `sys_role_menu` VALUES (15019, 1, 901341);
INSERT INTO `sys_role_menu` VALUES (15020, 1, 901342);
INSERT INTO `sys_role_menu` VALUES (15021, 1, 901343);
INSERT INTO `sys_role_menu` VALUES (15024, 1, 900001);
INSERT INTO `sys_role_menu` VALUES (15027, 1, 900012);
INSERT INTO `sys_role_menu` VALUES (15028, 1, 900013);
INSERT INTO `sys_role_menu` VALUES (15029, 1, 900014);
INSERT INTO `sys_role_menu` VALUES (15030, 1, 900015);
INSERT INTO `sys_role_menu` VALUES (15032, 1, 900017);
INSERT INTO `sys_role_menu` VALUES (15033, 1, 900018);
INSERT INTO `sys_role_menu` VALUES (15034, 1, 900019);
INSERT INTO `sys_role_menu` VALUES (15035, 1, 900020);
INSERT INTO `sys_role_menu` VALUES (15036, 1, 900021);
INSERT INTO `sys_role_menu` VALUES (15037, 1, 900022);
INSERT INTO `sys_role_menu` VALUES (15038, 1, 900023);
INSERT INTO `sys_role_menu` VALUES (15039, 1, 900114);
INSERT INTO `sys_role_menu` VALUES (15040, 1, 900115);
INSERT INTO `sys_role_menu` VALUES (15041, 1, 900215);
INSERT INTO `sys_role_menu` VALUES (15042, 1, 900216);
INSERT INTO `sys_role_menu` VALUES (15043, 1, 900217);
INSERT INTO `sys_role_menu` VALUES (15049, 1, 900417);
INSERT INTO `sys_role_menu` VALUES (15050, 1, 900418);
INSERT INTO `sys_role_menu` VALUES (15051, 1, 900419);
INSERT INTO `sys_role_menu` VALUES (15052, 1, 900420);
INSERT INTO `sys_role_menu` VALUES (15053, 1, 900518);
INSERT INTO `sys_role_menu` VALUES (15054, 1, 900519);
INSERT INTO `sys_role_menu` VALUES (15055, 1, 900520);
INSERT INTO `sys_role_menu` VALUES (15056, 1, 900521);
INSERT INTO `sys_role_menu` VALUES (15057, 1, 900522);
INSERT INTO `sys_role_menu` VALUES (15058, 1, 900619);
INSERT INTO `sys_role_menu` VALUES (15059, 1, 900620);
INSERT INTO `sys_role_menu` VALUES (15060, 1, 900621);
INSERT INTO `sys_role_menu` VALUES (15061, 1, 900622);
INSERT INTO `sys_role_menu` VALUES (15062, 1, 900623);
INSERT INTO `sys_role_menu` VALUES (15063, 1, 900720);
INSERT INTO `sys_role_menu` VALUES (15064, 1, 900721);
INSERT INTO `sys_role_menu` VALUES (15065, 1, 900722);
INSERT INTO `sys_role_menu` VALUES (15066, 1, 900723);
INSERT INTO `sys_role_menu` VALUES (15067, 1, 900724);
INSERT INTO `sys_role_menu` VALUES (15068, 1, 900821);
INSERT INTO `sys_role_menu` VALUES (15069, 1, 900822);
INSERT INTO `sys_role_menu` VALUES (15070, 1, 900823);
INSERT INTO `sys_role_menu` VALUES (15071, 1, 900824);
INSERT INTO `sys_role_menu` VALUES (15072, 1, 900825);
INSERT INTO `sys_role_menu` VALUES (15073, 1, 900922);
INSERT INTO `sys_role_menu` VALUES (15074, 1, 900923);
INSERT INTO `sys_role_menu` VALUES (15075, 1, 900924);
INSERT INTO `sys_role_menu` VALUES (15076, 1, 900925);
INSERT INTO `sys_role_menu` VALUES (15077, 1, 901023);
INSERT INTO `sys_role_menu` VALUES (15078, 1, 901024);
INSERT INTO `sys_role_menu` VALUES (15088, 1, 900024);
INSERT INTO `sys_role_menu` VALUES (15089, 1, 901124);
INSERT INTO `sys_role_menu` VALUES (15090, 1, 901125);
INSERT INTO `sys_role_menu` VALUES (15091, 1, 901126);
INSERT INTO `sys_role_menu` VALUES (15092, 1, 901127);
INSERT INTO `sys_role_menu` VALUES (15093, 1, 901128);
INSERT INTO `sys_role_menu` VALUES (15094, 1, 901129);
INSERT INTO `sys_role_menu` VALUES (15095, 1, 901130);
INSERT INTO `sys_role_menu` VALUES (15096, 1, 901131);
INSERT INTO `sys_role_menu` VALUES (15097, 1, 901132);
INSERT INTO `sys_role_menu` VALUES (15098, 1, 901133);
INSERT INTO `sys_role_menu` VALUES (15099, 1, 901134);
INSERT INTO `sys_role_menu` VALUES (15100, 1, 901135);
INSERT INTO `sys_role_menu` VALUES (15101, 1, 901136);
INSERT INTO `sys_role_menu` VALUES (15102, 1, 901137);
INSERT INTO `sys_role_menu` VALUES (15110, 1, 900026);
INSERT INTO `sys_role_menu` VALUES (15111, 1, 901326);
INSERT INTO `sys_role_menu` VALUES (15112, 1, 901327);
INSERT INTO `sys_role_menu` VALUES (15113, 1, 901328);
INSERT INTO `sys_role_menu` VALUES (15114, 1, 901329);
INSERT INTO `sys_role_menu` VALUES (15117, 1, 900027);
INSERT INTO `sys_role_menu` VALUES (15118, 1, 901330);
INSERT INTO `sys_role_menu` VALUES (15119, 1, 901331);
INSERT INTO `sys_role_menu` VALUES (15120, 1, 901332);
INSERT INTO `sys_role_menu` VALUES (15124, 1, 900028);
INSERT INTO `sys_role_menu` VALUES (15125, 1, 901333);
INSERT INTO `sys_role_menu` VALUES (15126, 1, 901334);
INSERT INTO `sys_role_menu` VALUES (15127, 1, 901335);
INSERT INTO `sys_role_menu` VALUES (15131, 1, 900029);
INSERT INTO `sys_role_menu` VALUES (15132, 1, 901336);
INSERT INTO `sys_role_menu` VALUES (15133, 1, 901337);
INSERT INTO `sys_role_menu` VALUES (15134, 1, 901338);
INSERT INTO `sys_role_menu` VALUES (15135, 1, 901339);
INSERT INTO `sys_role_menu` VALUES (15137, 1, 901350);
INSERT INTO `sys_role_menu` VALUES (15138, 1, 900031);
INSERT INTO `sys_role_menu` VALUES (15139, 1, 900100);
INSERT INTO `sys_role_menu` VALUES (15140, 1, 900101);
INSERT INTO `sys_role_menu` VALUES (15141, 1, 900102);
INSERT INTO `sys_role_menu` VALUES (15142, 1, 900103);
INSERT INTO `sys_role_menu` VALUES (15146, 1, 901351);
INSERT INTO `sys_role_menu` VALUES (15147, 1, 901352);
INSERT INTO `sys_role_menu` VALUES (15149, 1, 900032);
INSERT INTO `sys_role_menu` VALUES (15150, 1, 901353);
INSERT INTO `sys_role_menu` VALUES (15151, 1, 901354);
INSERT INTO `sys_role_menu` VALUES (15152, 1, 901355);
INSERT INTO `sys_role_menu` VALUES (15153, 1, 901356);
INSERT INTO `sys_role_menu` VALUES (15156, 1, 900034);
INSERT INTO `sys_role_menu` VALUES (15157, 1, 901360);
INSERT INTO `sys_role_menu` VALUES (15158, 1, 901361);
INSERT INTO `sys_role_menu` VALUES (15159, 1, 900035);
INSERT INTO `sys_role_menu` VALUES (15160, 1, 901370);
INSERT INTO `sys_role_menu` VALUES (15161, 1, 901371);
INSERT INTO `sys_role_menu` VALUES (15162, 1, 901372);
INSERT INTO `sys_role_menu` VALUES (15163, 1, 901373);
INSERT INTO `sys_role_menu` VALUES (15166, 1, 900036);
INSERT INTO `sys_role_menu` VALUES (15167, 1, 900316);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '密码',
  `salt` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '盐',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '邮箱',
  `mobile` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '手机号',
  `status` tinyint(0) NULL DEFAULT NULL COMMENT '状态  0：禁用   1：正常',
  `create_user_id` bigint(0) NULL DEFAULT NULL COMMENT '创建者ID',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `inviter_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '系统用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '1cc7f0f4e29524bfbfb2a299dde135e6ebad047b2c0ef70eceb7fbc198293927', 'YKJpTRPDurwTYVsIhd0P', 'root', '13612345678', 1, 1, '2016-11-11 11:11:11', NULL);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户ID',
  `role_id` bigint(0) NULL DEFAULT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户与角色对应关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (2, 4, 1);
INSERT INTO `sys_user_role` VALUES (3, 5, 1);
INSERT INTO `sys_user_role` VALUES (5, 7, 1);
INSERT INTO `sys_user_role` VALUES (7, 9, 1);
INSERT INTO `sys_user_role` VALUES (8, 8, 1);
INSERT INTO `sys_user_role` VALUES (10, 11, 1);
INSERT INTO `sys_user_role` VALUES (20, 10, 1);
INSERT INTO `sys_user_role` VALUES (22, 2, 1);
INSERT INTO `sys_user_role` VALUES (28, 6, 1);
INSERT INTO `sys_user_role` VALUES (29, 13, 1);
INSERT INTO `sys_user_role` VALUES (30, 12, 4);
INSERT INTO `sys_user_role` VALUES (31, 14, 1);
INSERT INTO `sys_user_role` VALUES (32, 15, 1);
INSERT INTO `sys_user_role` VALUES (33, 16, 1);
INSERT INTO `sys_user_role` VALUES (34, 1, 1);
INSERT INTO `sys_user_role` VALUES (36, 18, 2);

-- ----------------------------
-- Table structure for sys_user_token
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_token`;
CREATE TABLE `sys_user_token`  (
  `user_id` bigint(0) NOT NULL,
  `token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'token',
  `expire_time` datetime(0) NULL DEFAULT NULL COMMENT '过期时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `token`(`token`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '系统用户Token' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_token
-- ----------------------------
INSERT INTO `sys_user_token` VALUES (1, 'e298806306da27085dd489d1cac6f0e3', '2025-09-04 02:36:03', '2025-09-03 14:36:03');
INSERT INTO `sys_user_token` VALUES (2, '40c75bb5ecabcd58d85427ba273e053d', '2026-05-07 02:50:43', '2026-05-06 14:50:43');
INSERT INTO `sys_user_token` VALUES (10, '8cc6d3486f037055b6aec13a006a4ac5', '2026-01-08 22:01:15', '2026-01-08 10:01:15');
INSERT INTO `sys_user_token` VALUES (14, 'f7e44a416273d2c29c3c4960c212a406', '2025-09-23 22:47:30', '2025-09-23 10:47:30');
INSERT INTO `sys_user_token` VALUES (15, '37c8cd572124baf67cbf5e9cbcae9df7', '2026-04-28 04:29:21', '2026-04-27 16:29:21');
INSERT INTO `sys_user_token` VALUES (16, 'd07276328045391eee874799664b9e47', '2026-01-09 04:08:57', '2026-01-08 16:08:57');

-- ----------------------------
-- Table structure for tb_activity
-- ----------------------------
DROP TABLE IF EXISTS `tb_activity`;
CREATE TABLE `tb_activity`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `destination_id` bigint(0) NOT NULL COMMENT '目的地ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '活动名称',
  `address` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '活动地址',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度（地图选点）',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度（地图选点）',
  `price_from` decimal(12, 2) NULL DEFAULT NULL COMMENT '起步价/门票价',
  `tag` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签',
  `cover_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '活动介绍',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '活动开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '活动结束时间',
  `enroll_limit` int(0) NOT NULL DEFAULT 0 COMMENT '报名上限，0表示不限',
  `enroll_count` int(0) NOT NULL DEFAULT 0 COMMENT '已报名人数',
  `highlights` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '活动亮点 JSON 数组',
  `notices` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '参与须知 JSON 数组',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_activity_destination_id`(`destination_id`) USING BTREE,
  INDEX `idx_activity_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '精彩活动' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_activity
-- ----------------------------
INSERT INTO `tb_activity` VALUES (8, 4, '小寨CSOPLAY次元觉醒', '陕西省西安市雁塔区长安中路', 34.2217620, 108.9474310, 15.00, '[\"二次元\"]', 'https://tu.mianyazuju.com/2026/09/21/b2ac767d-2315-4380-99c1-b233416ab03a.png', '小寨CSOPLAY次元觉醒', '2026-09-22 00:00:00', '2026-09-23 00:00:00', 1, 1, '[{\"desc\":\"啊大苏打大苏打是\",\"label\":\"二次元\"}]', '[\"请提前到场\"]', 1, 1, '2026-09-21 10:25:33', '2026-09-21 11:17:55');

-- ----------------------------
-- Table structure for tb_activity_enrollment
-- ----------------------------
DROP TABLE IF EXISTS `tb_activity_enrollment`;
CREATE TABLE `tb_activity_enrollment`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activity_id` bigint(0) NOT NULL COMMENT '活动ID',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '报名时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_activity_enrollment_activity_user`(`activity_id`, `user_id`) USING BTREE,
  INDEX `idx_activity_enrollment_activity_id`(`activity_id`) USING BTREE,
  INDEX `idx_activity_enrollment_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '活动报名' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_activity_enrollment
-- ----------------------------
INSERT INTO `tb_activity_enrollment` VALUES (1, 8, 2, '2026-09-21 11:17:55');

-- ----------------------------
-- Table structure for tb_cancel_account
-- ----------------------------
DROP TABLE IF EXISTS `tb_cancel_account`;
CREATE TABLE `tb_cancel_account`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '申请时手机号快照',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '申请时昵称快照',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '注销原因',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'pending' COMMENT '状态：pending/approved/rejected',
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核备注/驳回原因',
  `audit_time` datetime(0) NULL DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '申请时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cancel_account_user`(`user_id`) USING BTREE,
  INDEX `idx_cancel_account_status`(`status`) USING BTREE,
  INDEX `idx_cancel_account_phone`(`phone`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户注销申请' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_cancel_account
-- ----------------------------

-- ----------------------------
-- Table structure for tb_canteen_reservation
-- ----------------------------
DROP TABLE IF EXISTS `tb_canteen_reservation`;
CREATE TABLE `tb_canteen_reservation`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `reservation_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '预约业务单号',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `merchant_id` bigint(0) NULL DEFAULT NULL COMMENT '商户ID',
  `order_id` bigint(0) NULL DEFAULT NULL COMMENT '关联商户订单ID',
  `destination_id` bigint(0) NULL DEFAULT NULL COMMENT '目的地ID',
  `shop_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '门店名称',
  `dish_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '预约菜品',
  `contact_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系电话',
  `guest_count` int(0) NOT NULL DEFAULT 1 COMMENT '用餐人数',
  `reserve_time` datetime(0) NOT NULL COMMENT '预约用餐时间',
  `address` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用餐地址',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'pending' COMMENT '状态：pending/confirmed/completed/cancelled',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_canteen_reservation_no`(`reservation_no`) USING BTREE,
  INDEX `idx_canteen_reservation_user`(`user_id`) USING BTREE,
  INDEX `idx_canteen_reservation_merchant`(`merchant_id`) USING BTREE,
  INDEX `idx_canteen_reservation_order`(`order_id`) USING BTREE,
  INDEX `idx_canteen_reservation_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '餐饮预约' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_canteen_reservation
-- ----------------------------
INSERT INTO `tb_canteen_reservation` VALUES (1, 'CR202609230930402991', 2, 7, 19, 1, '十八陕菜馆', NULL, '十六', '18892020826', 2, '2026-09-23 12:00:00', '陕西省西安市雁塔区慈恩路十八陕菜馆', NULL, 'pending', '2026-09-23 09:30:40', '2026-09-23 15:47:45');
INSERT INTO `tb_canteen_reservation` VALUES (2, 'CR202609231420264447', 3, 7, NULL, 1, '十八陕菜馆', NULL, '十七', '18892020827', 3, '2026-09-23 12:00:00', '陕西省西安市雁塔区慈恩路十八陕菜馆', NULL, 'cancelled', '2026-09-23 14:20:26', '2026-09-23 15:47:29');
INSERT INTO `tb_canteen_reservation` VALUES (3, 'CR202609231433185667', 3, 7, 20, 1, '十八陕菜馆', NULL, '十七', '18892020827', 2, '2026-09-23 12:00:00', '陕西省西安市雁塔区慈恩路十八陕菜馆', NULL, 'pending', '2026-09-23 14:33:19', '2026-09-23 15:47:45');
INSERT INTO `tb_canteen_reservation` VALUES (4, 'CR202609231523585366', 2, 7, 21, 1, '十八陕菜馆', NULL, '测试用户', '18892020826', 2, '2026-09-25 18:00:00', '陕西省西安市雁塔区慈恩路十八陕菜馆', NULL, 'pending', '2026-09-23 15:23:59', '2026-09-23 15:47:45');
INSERT INTO `tb_canteen_reservation` VALUES (5, 'CR202609231526355180', 2, 7, 22, 1, '十八陕菜馆', NULL, '排查测试A', '18892020826', 2, '2026-09-26 12:00:00', '陕西省西安市雁塔区慈恩路十八陕菜馆', NULL, 'pending', '2026-09-23 15:26:35', '2026-09-23 15:47:45');
INSERT INTO `tb_canteen_reservation` VALUES (6, 'CR202609231526354618', 2, 7, 23, 1, '十八陕菜馆', NULL, '排查测试B', '18892020826', 2, '2026-09-26 18:00:00', '陕西省西安市雁塔区慈恩路十八陕菜馆', NULL, 'confirmed', '2026-09-23 15:26:35', '2026-09-23 15:47:45');
INSERT INTO `tb_canteen_reservation` VALUES (7, 'CR202609231620525572', 3, 7, 26, 4, '十八陕菜馆', NULL, '叁拾', '18892020827', 2, '2026-09-23 12:00:00', '陕西省西安市雁塔区慈恩路十八陕菜馆', NULL, 'completed', '2026-09-23 16:20:53', '2026-09-23 16:20:52');

-- ----------------------------
-- Table structure for tb_commission_record
-- ----------------------------
DROP TABLE IF EXISTS `tb_commission_record`;
CREATE TABLE `tb_commission_record`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `beneficiary_user_id` bigint(0) NOT NULL COMMENT '受益人用户ID',
  `source_user_id` bigint(0) NOT NULL COMMENT '下单用户ID',
  `order_id` bigint(0) NOT NULL COMMENT '订单ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号',
  `commission_level` tinyint(0) NOT NULL COMMENT '佣金层级：1一级 2二级',
  `order_amount` decimal(12, 2) NOT NULL COMMENT '订单实付金额',
  `commission_rate` decimal(8, 4) NOT NULL COMMENT '佣金比例',
  `commission_amount` decimal(12, 2) NOT NULL COMMENT '佣金金额',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'settled' COMMENT '状态：settled',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_commission_order_level_beneficiary`(`order_id`, `commission_level`, `beneficiary_user_id`) USING BTREE,
  INDEX `idx_commission_beneficiary_user_id`(`beneficiary_user_id`) USING BTREE,
  INDEX `idx_commission_source_user_id`(`source_user_id`) USING BTREE,
  INDEX `idx_commission_order_no`(`order_no`) USING BTREE,
  INDEX `idx_commission_level`(`commission_level`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '邀请佣金记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_commission_record
-- ----------------------------
INSERT INTO `tb_commission_record` VALUES (1, 1, 2, 9, 'HT1789704334658237', 1, 256.00, 0.0500, 12.80, 'settled', '2026-09-18 12:05:35', '2026-09-18 12:05:34');
INSERT INTO `tb_commission_record` VALUES (2, 2, 4, 27, 'OD1790227052165352', 1, 23.90, 0.0500, 1.20, 'settled', '2026-09-24 15:28:11', '2026-09-24 15:28:11');
INSERT INTO `tb_commission_record` VALUES (3, 1, 4, 27, 'OD1790227052165352', 2, 23.90, 0.0200, 0.48, 'settled', '2026-09-24 15:28:11', '2026-09-24 15:28:11');

-- ----------------------------
-- Table structure for tb_coupon_template
-- ----------------------------
DROP TABLE IF EXISTS `tb_coupon_template`;
CREATE TABLE `tb_coupon_template`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板业务编码',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '优惠券名称',
  `amount` decimal(12, 2) NOT NULL COMMENT '优惠金额',
  `min_amount` decimal(12, 2) NOT NULL COMMENT '最低消费门槛',
  `scope` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'all' COMMENT '适用范围：all/hotel/route/goods',
  `valid_days` int(0) NOT NULL DEFAULT 30 COMMENT '领取后有效天数',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '使用说明',
  `total_count` int(0) NOT NULL DEFAULT 0 COMMENT '发放总量，0不限',
  `claimed_count` int(0) NOT NULL DEFAULT 0 COMMENT '已领取数量',
  `per_user_limit` int(0) NOT NULL DEFAULT 1 COMMENT '每人限领张数',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1可领取 0下架',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_coupon_template_code`(`template_code`) USING BTREE,
  INDEX `idx_coupon_template_status`(`status`) USING BTREE,
  INDEX `idx_coupon_template_scope`(`scope`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '优惠券模板' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_coupon_template
-- ----------------------------
INSERT INTO `tb_coupon_template` VALUES (1, 'tpl-all-10', '新人满100减10', 10.00, 100.00, 'all', 30, '全场订单可用，每人限领1张', 0, 2, 1, 1, '2026-09-10 09:43:33', '2026-09-24 13:04:42');
INSERT INTO `tb_coupon_template` VALUES (2, 'tpl-hotel-30', '酒店满300减30', 30.00, 300.00, 'hotel', 60, '仅限酒店预订使用', 0, 2, 1, 1, '2026-09-10 09:43:33', '2026-09-24 13:04:41');
INSERT INTO `tb_coupon_template` VALUES (3, 'tpl-route-15', '线路满50减15', 15.00, 50.00, 'route', 45, '仅限旅游线路预约', 0, 1, 1, 1, '2026-09-10 09:43:33', '2026-09-24 13:04:40');
INSERT INTO `tb_coupon_template` VALUES (4, 'tpl-goods-20', '好物满80减20', 20.00, 80.00, 'goods', 30, '仅限非遗好物购买', 0, 2, 1, 1, '2026-09-10 09:43:33', '2026-09-24 13:04:40');
INSERT INTO `tb_coupon_template` VALUES (5, 'tpl-all-5', '周末特惠5元券', 5.00, 30.00, 'all', 7, '无门槛小额券，每人限领1张', 0, 2, 1, 1, '2026-09-10 09:43:33', '2026-09-24 13:04:40');

-- ----------------------------
-- Table structure for tb_destination
-- ----------------------------
DROP TABLE IF EXISTS `tb_destination`;
CREATE TABLE `tb_destination`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `destination_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目的地业务编码，如 rongchang',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目的地全称',
  `short_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '简称',
  `intro` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '简介',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1已上线 2即将上线 0禁用',
  `is_default` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否默认目的地：1是 0否',
  `merchant_count` int(0) NOT NULL DEFAULT 0 COMMENT '商户数量（冗余统计）',
  `resource_count` int(0) NOT NULL DEFAULT 0 COMMENT '资源数量（冗余统计）',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序，升序',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_destination_code`(`destination_code`) USING BTREE,
  INDEX `idx_destination_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '目的地' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_destination
-- ----------------------------
INSERT INTO `tb_destination` VALUES (1, 'rongchang', '荣昌区', '荣昌', '卤鹅、陶艺、夏布与千年古镇', 1, 1, 14, 32, 1, '2026-09-10 09:43:32', '2026-09-16 09:58:25');
INSERT INTO `tb_destination` VALUES (2, 'wanzhou', '万州区', '万州', '山水城市与三峡风情', 2, 0, 0, 0, 2, '2026-09-10 09:43:32', '2026-09-10 09:43:32');
INSERT INTO `tb_destination` VALUES (3, 'yongchuan', '永川区', '永川', '茶山竹海与城市休闲', 2, 0, 0, 0, 3, '2026-09-10 09:43:32', '2026-09-10 09:43:32');
INSERT INTO `tb_destination` VALUES (4, 'xian', '西安市', '西安', '十三朝古都，丝绸之路起点；兵马俑、大雁塔、古城墙与大唐不夜城交相辉映，承载周秦汉唐文明与关中非遗。', 1, 0, 4, 18, 4, '2026-09-10 09:44:16', '2026-09-23 15:42:54');

-- ----------------------------
-- Table structure for tb_favorite
-- ----------------------------
DROP TABLE IF EXISTS `tb_favorite`;
CREATE TABLE `tb_favorite`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收藏对象类型：scenic/hotel/route/goods/goose/activity',
  `target_id` bigint(0) NOT NULL COMMENT '收藏对象业务ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '收藏时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_favorite_user_target`(`user_id`, `target_type`, `target_id`) USING BTREE,
  INDEX `idx_favorite_user_time`(`user_id`, `create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户收藏' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_favorite
-- ----------------------------
INSERT INTO `tb_favorite` VALUES (1, 2, 'hotel', 5, '2026-09-17 10:44:23', '2026-09-17 10:44:23');
INSERT INTO `tb_favorite` VALUES (2, 2, 'scenic', 5, '2026-09-21 13:27:52', '2026-09-21 13:27:51');
INSERT INTO `tb_favorite` VALUES (3, 2, 'scenic', 8, '2026-09-21 13:39:57', '2026-09-21 13:39:56');
INSERT INTO `tb_favorite` VALUES (4, 2, 'route', 6, '2026-09-22 09:23:27', '2026-09-22 09:23:26');
INSERT INTO `tb_favorite` VALUES (5, 5, 'goods', 1, '2026-09-24 11:13:39', '2026-09-24 11:13:38');
INSERT INTO `tb_favorite` VALUES (6, 4, 'goods', 1, '2026-09-24 11:35:36', '2026-09-24 11:35:35');

-- ----------------------------
-- Table structure for tb_feedback
-- ----------------------------
DROP TABLE IF EXISTS `tb_feedback`;
CREATE TABLE `tb_feedback`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '反馈类型：功能异常/界面体验/订单问题/建议反馈/其他',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '反馈内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '截图URL列表（JSON数组）',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系电话',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'pending' COMMENT '处理状态：pending/processed',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_feedback_user`(`user_id`) USING BTREE,
  INDEX `idx_feedback_type`(`type`) USING BTREE,
  INDEX `idx_feedback_status`(`status`) USING BTREE,
  INDEX `idx_feedback_phone`(`contact_phone`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户意见反馈' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_feedback
-- ----------------------------

-- ----------------------------
-- Table structure for tb_footprint
-- ----------------------------
DROP TABLE IF EXISTS `tb_footprint`;
CREATE TABLE `tb_footprint`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '浏览对象类型：scenic/hotel/route/goods/goose/activity',
  `target_id` bigint(0) NOT NULL COMMENT '浏览对象业务ID',
  `visit_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '最近浏览时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '首次浏览时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_footprint_user_target`(`user_id`, `target_type`, `target_id`) USING BTREE,
  INDEX `idx_footprint_user_visit`(`user_id`, `visit_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户浏览足迹' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_footprint
-- ----------------------------
INSERT INTO `tb_footprint` VALUES (1, 2, 'hotel', 5, '2026-09-24 16:46:02', '2026-09-16 20:13:42', '2026-09-16 20:13:41');
INSERT INTO `tb_footprint` VALUES (2, 2, 'activity', 5, '2026-09-17 16:59:00', '2026-09-17 14:51:38', '2026-09-17 14:51:37');
INSERT INTO `tb_footprint` VALUES (3, 2, 'scenic', 5, '2026-09-23 10:26:02', '2026-09-17 17:00:45', '2026-09-17 17:00:44');
INSERT INTO `tb_footprint` VALUES (4, 2, 'activity', 8, '2026-09-21 11:23:43', '2026-09-21 10:26:05', '2026-09-21 10:26:05');
INSERT INTO `tb_footprint` VALUES (5, 2, 'scenic', 8, '2026-09-21 14:58:28', '2026-09-21 13:39:49', '2026-09-21 13:39:48');
INSERT INTO `tb_footprint` VALUES (6, 2, 'scenic', 10, '2026-09-21 14:58:45', '2026-09-21 14:58:45', '2026-09-21 14:58:45');
INSERT INTO `tb_footprint` VALUES (7, 2, 'route', 5, '2026-09-21 15:32:46', '2026-09-21 15:15:21', '2026-09-21 15:15:21');
INSERT INTO `tb_footprint` VALUES (8, 3, 'route', 5, '2026-09-21 17:18:25', '2026-09-21 15:35:47', '2026-09-21 15:35:46');
INSERT INTO `tb_footprint` VALUES (9, 3, 'hotel', 5, '2026-09-23 19:56:39', '2026-09-21 15:36:10', '2026-09-21 15:36:10');
INSERT INTO `tb_footprint` VALUES (10, 3, 'route', 6, '2026-09-22 13:16:26', '2026-09-21 17:55:15', '2026-09-21 17:55:14');
INSERT INTO `tb_footprint` VALUES (11, 2, 'route', 6, '2026-09-24 15:23:43', '2026-09-21 17:57:26', '2026-09-21 17:57:26');
INSERT INTO `tb_footprint` VALUES (12, 2, 'route', 7, '2026-09-23 11:23:23', '2026-09-22 13:35:19', '2026-09-22 13:35:19');
INSERT INTO `tb_footprint` VALUES (13, 3, 'route', 7, '2026-09-22 13:44:14', '2026-09-22 13:42:59', '2026-09-22 13:42:59');
INSERT INTO `tb_footprint` VALUES (14, 3, 'goose', 7, '2026-09-23 16:52:52', '2026-09-22 14:12:34', '2026-09-22 14:12:34');
INSERT INTO `tb_footprint` VALUES (15, 2, 'goose', 7, '2026-09-23 15:23:58', '2026-09-22 18:08:13', '2026-09-22 18:08:13');
INSERT INTO `tb_footprint` VALUES (16, 3, 'scenic', 5, '2026-09-23 19:56:21', '2026-09-23 19:56:21', '2026-09-23 19:56:20');
INSERT INTO `tb_footprint` VALUES (17, 5, 'scenic', 5, '2026-09-24 16:58:37', '2026-09-23 20:32:18', '2026-09-23 20:32:18');
INSERT INTO `tb_footprint` VALUES (18, 5, 'scenic', 6, '2026-09-23 20:32:27', '2026-09-23 20:32:27', '2026-09-23 20:32:26');
INSERT INTO `tb_footprint` VALUES (19, 5, 'scenic', 7, '2026-09-23 20:32:32', '2026-09-23 20:32:32', '2026-09-23 20:32:32');
INSERT INTO `tb_footprint` VALUES (20, 5, 'scenic', 8, '2026-09-23 20:32:41', '2026-09-23 20:32:41', '2026-09-23 20:32:40');
INSERT INTO `tb_footprint` VALUES (21, 5, 'scenic', 10, '2026-09-23 20:32:48', '2026-09-23 20:32:48', '2026-09-23 20:32:47');
INSERT INTO `tb_footprint` VALUES (22, 5, 'goods', 1, '2026-09-24 11:13:28', '2026-09-24 11:13:28', '2026-09-24 11:13:27');
INSERT INTO `tb_footprint` VALUES (23, 4, 'goods', 1, '2026-09-24 15:32:15', '2026-09-24 11:30:05', '2026-09-24 11:30:04');
INSERT INTO `tb_footprint` VALUES (24, 4, 'activity', 8, '2026-09-24 15:38:52', '2026-09-24 15:38:52', '2026-09-24 15:38:52');
INSERT INTO `tb_footprint` VALUES (25, 2, 'goods', 1, '2026-09-24 16:46:13', '2026-09-24 15:51:08', '2026-09-24 15:51:07');
INSERT INTO `tb_footprint` VALUES (26, 5, 'goose', 7, '2026-09-24 16:14:37', '2026-09-24 16:14:28', '2026-09-24 16:14:27');

-- ----------------------------
-- Table structure for tb_help_faq
-- ----------------------------
DROP TABLE IF EXISTS `tb_help_faq`;
CREATE TABLE `tb_help_faq`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类编码：booking/account/payment/service',
  `category_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称（展示用）',
  `question` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '问题标题',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '问题答案',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序，升序',
  `state` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_help_faq_category`(`category_code`) USING BTREE,
  INDEX `idx_help_faq_state`(`state`) USING BTREE,
  INDEX `idx_help_faq_sort`(`sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '帮助中心FAQ' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_help_faq
-- ----------------------------
INSERT INTO `tb_help_faq` VALUES (1, 'booking', '预约与订单', '如何预约景区或活动？', '在首页或对应模块浏览目标景区/活动，进入详情页后点击「立即预约」，按提示填写信息并提交即可。预约结果可在「我的订单」中查看。', 1, 1, '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_help_faq` VALUES (2, 'account', '账户与设置', '如何切换浏览地区？', '在首页顶部点击当前地区名称，进入地区选择页即可切换。切换后首页推荐、景区列表等内容会同步更新。', 1, 1, '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_help_faq` VALUES (3, 'booking', '预约与订单', '订单可以取消或退款吗？', '未使用的订单可在「我的订单」中申请取消。具体退款规则以各服务详情页说明为准，如有疑问请联系客服。', 2, 1, '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_help_faq` VALUES (4, 'payment', '支付与优惠', '优惠券如何使用？', '在「我的-优惠券」中查看可用券，下单时在确认页选择对应优惠券即可抵扣。每张券有使用条件和有效期，请注意查看。', 1, 1, '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_help_faq` VALUES (5, 'service', '客服帮助', '如何联系客服？', '您可通过「我的-客服中心」拨打客服电话、添加微信或发起在线咨询，工作时间内我们将尽快为您答复。', 1, 1, '2026-09-10 09:44:08', '2026-09-10 09:44:08');

-- ----------------------------
-- Table structure for tb_heritage_craft
-- ----------------------------
DROP TABLE IF EXISTS `tb_heritage_craft`;
CREATE TABLE `tb_heritage_craft`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `destination_id` bigint(0) NULL DEFAULT NULL COMMENT '目的地ID',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度（地图选点）',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度（地图选点）',
  `address` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目地址',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工艺名称',
  `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '短描述',
  `level_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '级别编码：national/provincial/municipal',
  `level_label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '级别展示文案',
  `tag` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '工艺分类标签',
  `shop_category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '好店分类：pottery/textile/culture',
  `tags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '分类标签JSON数组',
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标标识',
  `cover_from` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面渐变起始色',
  `cover_to` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面渐变结束色',
  `cover_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `banner_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '轮播图JSON数组',
  `overview` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '概述',
  `feature` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '工艺特点',
  `culture` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '文化价值',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_heritage_craft_destination_id`(`destination_id`) USING BTREE,
  INDEX `idx_heritage_craft_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '非遗工艺' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_heritage_craft
-- ----------------------------
INSERT INTO `tb_heritage_craft` VALUES (1, 4, 34.2526620, 108.9444190, '陕西省西安市碑林区湘子庙街6号', '西安鼓乐', '传承千年的唐代宫廷乐遗存，被誉为“中国古代音乐活化石”', 'world', '世界级（联合国教科文组织人类非物质文化遗产）', '传统音乐', 'c_34da4875', '[\"传统音乐\",\"表演艺术\",\"民俗仪式\"]', NULL, NULL, NULL, 'https://tu.mianyazuju.com/2026/09/24/8c2de72f-d12c-42ef-a49a-74be4752cee0.png', '[\"https://tu.mianyazuju.com/2026/09/24/3fa2277d-f9ab-4853-9927-47fbbb7a1558.png\",\"https://tu.mianyazuju.com/2026/09/24/a5b6f40c-f0a1-4386-9cb1-d0953cbd7376.png\",\"https://tu.mianyazuju.com/2026/09/24/3f419498-acf3-4aa4-9888-3be972c77ae8.jpg\",\"https://tu.mianyazuju.com/2026/09/24/0437433a-3e6f-40fb-b1af-32ef74cef238.jpg\"]', '西安鼓乐在西安地区流传千年以上，是目前中国现存最古老的大型民间乐种之一，2009年被列入联合国教科文组织《人类非物质文化遗产代表作名录》，也是西安唯一的世界级非遗项目。', '使用笙、管、笛、鼓、锣等传统乐器，乐谱采用唐宋时期的古老工尺谱记录，现存约3000首乐曲、150余卷手写谱本；分为“坐乐”和“行乐”两种演奏形式，行乐还包含男声合唱，演奏场合多为庙会、葬礼等传统民俗活动。', '完整保留了唐代宫廷音乐的演奏形式与曲目体系，是研究中国古代音乐史、唐宋社会民俗的活态史料。', 1, 0, '2026-09-24 10:27:01', '2026-09-24 10:54:09');
INSERT INTO `tb_heritage_craft` VALUES (2, 4, 34.0654710, 108.8268790, '陕西省西安市长安区滦镇东街', '楮皮纸制作技艺', '比蔡伦造纸还早300年的手工造纸活化石', 'national', '国家级', '传统技艺', 'craft', '[\"传统技艺\",\"手工造纸\",\"古法工艺\"]', NULL, NULL, NULL, 'https://tu.mianyazuju.com/2026/09/24/ec44cdfc-e207-463f-a153-42994c88d671.jpg', '[\"https://tu.mianyazuju.com/2026/09/24/e71e9262-10a3-4692-a2ab-e80667a32126.webp\",\"https://tu.mianyazuju.com/2026/09/24/8b0e6018-dc29-4ec9-9534-5f2017ac28b3.png\",\"https://tu.mianyazuju.com/2026/09/24/950ff077-9d56-415a-9930-79bd06e0f926.jpeg\",\"https://tu.mianyazuju.com/2026/09/24/efd22162-4d54-4678-aa9a-4af4202c4d2e.jpg\",\"https://tu.mianyazuju.com/2026/09/24/759fa7aa-3b8e-4e5c-b2b2-8709b6b80c0d.jpg\"]', '流传于西安长安区北张村一带，经专家考证其造纸技艺比东汉蔡伦造纸还要早300年，是手工纸工艺演化进程的“活化石”，入选国家级非遗名录。', '完全沿用古法手工工序，以秦岭野生楮树皮为原料，历经浸泡、清洗、发酵、漂白、打浆、抄纸、晒纸等数十道工序，全程不依赖现代机械设备，纸张坚韧光滑、吸水性极佳，可长期保存不腐。', '见证了中国造纸术的早期形态，是研究古代手工造纸技术演变的重要实物依据，也是关中地区传统农耕文明与手工业结合的典型代表。', 1, 0, '2026-09-24 13:08:49', '2026-09-24 13:08:49');
INSERT INTO `tb_heritage_craft` VALUES (3, 4, 34.2614640, 108.9495970, '陕西省西安市新城区西一路', '秦腔', '中国最古老的戏曲剧种之一，西北文化的灵魂载体', 'national', '世界级（联合国教科文组织人类非物质文化遗产）', '传统戏剧', 'c_34da4875', '[\"传统戏剧\",\"戏曲艺术\",\"地方文化\"]', NULL, NULL, NULL, 'https://tu.mianyazuju.com/2026/09/24/c215773e-48b3-412a-a279-9bb8f69298ec.jpg', '[\"https://tu.mianyazuju.com/2026/09/24/981f6135-cb5b-4c6c-b852-703f21346865.webp\",\"https://tu.mianyazuju.com/2026/09/24/08a0d408-365c-4709-9e4b-1e2970a46ed9.jpeg\",\"https://tu.mianyazuju.com/2026/09/24/1b2725a3-3f01-47f4-b8ad-7c645159b603.jpg\",\"https://tu.mianyazuju.com/2026/09/24/e07af5a6-6ca7-474f-883c-79c18ada8f82.jpg\",\"https://tu.mianyazuju.com/2026/09/24/d3fdece3-3165-4bc9-98f6-8c97857e047f.jpg\"]', '秦腔起源于陕西关中地区，是中国现存最古老的戏曲剧种之一，被誉为“百戏之祖”，对京剧、豫剧等多个剧种的形成产生了深远影响，入选国家级非遗名录。', '唱腔高亢激昂、慷慨悲壮，表演程式完整，包含唱、念、做、打全套技艺；脸谱体系独特，通过不同色彩和图案刻画人物性格；伴奏以板胡为核心乐器，配合锣鼓、梆子等打击乐，形成独特的“秦声秦韵”。', '承载了西北地区千百年来的民间情感与历史记忆，是研究中国戏曲发展史、西北民俗文化的核心载体。', 1, 0, '2026-09-24 13:13:33', '2026-09-24 13:13:33');
INSERT INTO `tb_heritage_craft` VALUES (4, 4, 34.2602640, 108.9452180, '陕西省西安市莲湖区西大街3号', '中华老字号同盛祥牛羊肉泡馍制作技艺', '传承百年的长安味道，国家级非遗美食', 'national', '国家级', '传统技艺', 'food', '[\"传统技艺\",\"美食制作\",\"中华老字号\"]', NULL, NULL, NULL, 'https://tu.mianyazuju.com/2026/09/24/f060dd2f-db2f-4e13-8523-5a3c9f2c41bf.jpeg', '[\"https://tu.mianyazuju.com/2026/09/24/768cd734-1b23-4a1e-a650-6692546df1a7.png\",\"https://tu.mianyazuju.com/2026/09/24/c6843cb2-bab8-488c-8c6b-83f0cc26666b.png\",\"https://tu.mianyazuju.com/2026/09/24/f8524279-4738-4bdf-bb86-6daf79b911f1.webp\",\"https://tu.mianyazuju.com/2026/09/24/22c226da-4562-41b8-9a00-d5892d63a35c.jpeg\",\"https://tu.mianyazuju.com/2026/09/24/397714c0-d1ee-40ea-81c5-7a2f4d7533de.jpg\",\"https://tu.mianyazuju.com/2026/09/24/7f04b888-0101-4283-b711-3178be774553.jpg\"]', '同盛祥始创于1920年，其牛羊肉泡馍制作技艺入选国家级非遗名录，是西安最具代表性的传统美食之一。', '核心工序包括选料、熬汤、烙馍、掰馍、煮制五大环节，汤料需选用优质牛羊肉经长时间熬制，馍需手工烙成九分熟，食客自行掰成黄豆大小后由厨师回锅煮制，讲究“汤浓肉烂、馍筋光滑”。', '浓缩了西安回汉民族饮食文化的融合智慧，是长安饮食文化的标志性符号，也是西安城市烟火气的重要体现。', 1, 0, '2026-09-24 13:16:17', '2026-09-24 13:16:16');

-- ----------------------------
-- Table structure for tb_heritage_goods
-- ----------------------------
DROP TABLE IF EXISTS `tb_heritage_goods`;
CREATE TABLE `tb_heritage_goods`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` bigint(0) NOT NULL COMMENT '关联非遗店铺ID',
  `merchant_service_id` bigint(0) NULL DEFAULT NULL COMMENT '关联商户服务ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
  `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列表短描述',
  `cover_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `price` decimal(10, 2) NOT NULL COMMENT '售价（元）',
  `sales` int(0) NOT NULL DEFAULT 0 COMMENT '销量',
  `stock` int(0) NOT NULL DEFAULT 0 COMMENT '库存',
  `rating` decimal(2, 1) NULL DEFAULT NULL COMMENT '评分',
  `category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类：featured/hot/new',
  `specs` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '规格JSON数组',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '详情介绍',
  `banners` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '轮播图JSON数组',
  `detail_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '商品详情图JSON数组',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_heritage_goods_merchant_service`(`merchant_service_id`) USING BTREE,
  INDEX `idx_heritage_goods_shop_id`(`shop_id`) USING BTREE,
  INDEX `idx_heritage_goods_category`(`category`) USING BTREE,
  INDEX `idx_heritage_goods_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '非遗商品' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_heritage_goods
-- ----------------------------
INSERT INTO `tb_heritage_goods` VALUES (1, 1, 19, '西安毛绒玩具文创', '好物简介西安毛绒玩具文创多种品类西安毛绒玩具文创多种品类西安毛绒玩具文创多种品类西安毛绒玩具文创多种品类西安毛绒玩具文创多种品类', 'https://tu.mianyazuju.com/2026/09/23/57b83633-34e9-476c-bde9-c0e0e1b5d249.png', 15.90, 0, 90, NULL, 'featured', '[{\"name\":\"大雁塔毛绒文创\",\"price\":15.9,\"stock\":44},{\"name\":\"秦岭大熊猫文创\",\"price\":25.9,\"stock\":46}]', '西安毛绒玩具文创多种品类西安毛绒玩具文创多种品类西安毛绒玩具文创多种品类西安毛绒玩具文创多种品类西安毛绒玩具文创多种品类', '[{\"image\":\"https://tu.mianyazuju.com/2026/09/23/e542f1b5-111d-4eda-b4ef-4aaaa3c2c8dd.png\"},{\"image\":\"https://tu.mianyazuju.com/2026/09/23/9eddf09e-4e0d-4abe-8ecb-0fd0c154c9c6.png\"},{\"image\":\"https://tu.mianyazuju.com/2026/09/23/1f51748d-90e5-4443-8cd0-1631a300ee15.webp\"}]', '[\"https://tu.mianyazuju.com/2026/09/23/bbc9b883-03de-40d8-a569-dcfba5d05fff.png\"]', 1, 0, '2026-09-23 19:27:21', '2026-09-23 19:27:21');

-- ----------------------------
-- Table structure for tb_heritage_goods_review
-- ----------------------------
DROP TABLE IF EXISTS `tb_heritage_goods_review`;
CREATE TABLE `tb_heritage_goods_review`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goods_id` bigint(0) NOT NULL COMMENT '商品ID',
  `order_id` bigint(0) NULL DEFAULT NULL COMMENT '关联订单ID（一单一评）',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户ID（可为空）',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评价用户展示名',
  `content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评价内容',
  `rating` tinyint(0) NULL DEFAULT NULL COMMENT '评分1-5',
  `images` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '评价图片URL列表(JSON)',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_heritage_goods_review_order_user`(`order_id`, `user_id`) USING BTREE,
  INDEX `idx_heritage_goods_review_goods_id`(`goods_id`) USING BTREE,
  INDEX `idx_heritage_goods_review_order_id`(`order_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '非遗商品评价' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_heritage_goods_review
-- ----------------------------

-- ----------------------------
-- Table structure for tb_heritage_shop
-- ----------------------------
DROP TABLE IF EXISTS `tb_heritage_shop`;
CREATE TABLE `tb_heritage_shop`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id` bigint(0) NULL DEFAULT NULL COMMENT '关联商户ID',
  `destination_id` bigint(0) NULL DEFAULT NULL COMMENT '目的地ID',
  `craft_id` bigint(0) NULL DEFAULT NULL COMMENT '关联非遗工艺ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '店铺名称',
  `address` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地址',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度（地图选点）',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度（地图选点）',
  `tag` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '主标签',
  `tags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '标签JSON数组',
  `category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类：pottery/textile/culture',
  `intro` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '简介',
  `intro_detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '详细介绍',
  `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列表短描述',
  `cover_from` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面渐变起始色',
  `cover_to` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面渐变结束色',
  `cover_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `banner_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '轮播图JSON数组',
  `shipping_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '运费配置JSON',
  `business_hours` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业时间',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `rating` decimal(2, 1) NULL DEFAULT NULL COMMENT '评分',
  `review_count` int(0) NOT NULL DEFAULT 0 COMMENT '评价数',
  `services` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '服务项目JSON数组',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1营业 0歇业',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_heritage_shop_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_heritage_shop_destination_id`(`destination_id`) USING BTREE,
  INDEX `idx_heritage_shop_craft_id`(`craft_id`) USING BTREE,
  INDEX `idx_heritage_shop_category`(`category`) USING BTREE,
  INDEX `idx_heritage_shop_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '非遗店铺' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_heritage_shop
-- ----------------------------
INSERT INTO `tb_heritage_shop` VALUES (1, 8, 4, NULL, '叁拾文创专卖店', '陕西省西安市雁塔区科技六路叁拾文创专卖店', 34.2103190, 108.8723910, '非遗文创', '[\"非遗文创\",\"可体验\"]', 'textile', '叁拾文创专卖店专注荣昌非遗文创与手作体验。', '叁拾文创专卖店专注荣昌非遗文创与手作体验。', NULL, NULL, NULL, 'https://tu.mianyazuju.com/2026/09/23/f1ccf878-bb3f-44d2-a0c1-47f7f439c794.webp', '[\"https://tu.mianyazuju.com/2026/09/24/0f4e4aef-4978-45e3-8aa7-fd24cb52430b.png\",\"https://tu.mianyazuju.com/2026/09/24/e1710c33-108e-4be9-8ce8-b609a92a3181.jpeg\",\"https://tu.mianyazuju.com/2026/09/24/c4fa75ba-a94e-4362-9659-475ade3a55bb.webp\",\"https://tu.mianyazuju.com/2026/09/24/f5c4ad9d-0310-4b21-8412-d85f1c2d1e88.jpg\"]', '{\"freeShipping\":false,\"fixedFee\":8.00,\"freeAmountThreshold\":0.00,\"shippingNote\":\"新疆西藏等偏远地区请联系客服\"}', '09:00-21:00', '18892020830', 4.8, 0, '[\"文创销售\",\"手作体验\"]', 1, 0, '2026-09-23 17:42:14', '2026-09-24 11:09:44');

-- ----------------------------
-- Table structure for tb_heritage_shop_category
-- ----------------------------
DROP TABLE IF EXISTS `tb_heritage_shop_category`;
CREATE TABLE `tb_heritage_shop_category`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类编码（业务键）',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_heritage_shop_category_code`(`code`) USING BTREE,
  INDEX `idx_heritage_shop_category_status_sort`(`status`, `sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '非遗好店分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_heritage_shop_category
-- ----------------------------
INSERT INTO `tb_heritage_shop_category` VALUES (1, 'pottery', '陶艺', 10, 1, '2026-09-24 10:15:15', '2026-09-24 10:15:15');
INSERT INTO `tb_heritage_shop_category` VALUES (2, 'textile', '纺织', 20, 1, '2026-09-24 10:15:15', '2026-09-24 10:15:15');
INSERT INTO `tb_heritage_shop_category` VALUES (3, 'culture', '综合', 30, 1, '2026-09-24 10:15:15', '2026-09-24 10:15:15');
INSERT INTO `tb_heritage_shop_category` VALUES (4, 'food', '美食', 40, 1, '2026-09-24 10:15:15', '2026-09-24 10:15:15');
INSERT INTO `tb_heritage_shop_category` VALUES (5, 'craft', '手工艺', 50, 1, '2026-09-24 10:15:15', '2026-09-24 10:15:15');
INSERT INTO `tb_heritage_shop_category` VALUES (6, 'c_34da4875', '鼓乐', 0, 1, '2026-09-24 10:24:55', '2026-09-24 10:24:59');

-- ----------------------------
-- Table structure for tb_home_module
-- ----------------------------
DROP TABLE IF EXISTS `tb_home_module`;
CREATE TABLE `tb_home_module`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `module_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'grid=宫格 tab=推荐Tab',
  `module_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务键',
  `label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '展示名称',
  `icon` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标名或图片URL',
  `icon_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标颜色',
  `bg_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '背景色',
  `link_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '跳转路径',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序升序',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `destination_id` bigint(0) NULL DEFAULT NULL COMMENT 'NULL=全平台',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_home_module_type_status`(`module_type`, `status`, `sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '首页宫格与推荐Tab配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_home_module
-- ----------------------------
INSERT INTO `tb_home_module` VALUES (1, 'grid', 'goose', '西安美食', 'https://tu.mianyazuju.com/2026/09/16/69dcdd09-10ab-469b-ac3b-033b661853cc.png', '#FF7A00', '#FFF0E6', '/mobile-design/goose/list', 1, 1, NULL, '2026-09-10 09:35:42', '2026-09-17 15:32:56');
INSERT INTO `tb_home_module` VALUES (2, 'grid', 'scenic', '旅游景区', 'https://tu.mianyazuju.com/2026/09/16/096f9900-149e-4b05-93cf-58dbb033e7ee.png', '#2EAF6A', '#E8F8EF', '/mobile-design/scenic/list', 2, 1, NULL, '2026-09-10 09:35:42', '2026-09-10 09:35:42');
INSERT INTO `tb_home_module` VALUES (3, 'grid', 'hotel', '酒店住宿', 'https://tu.mianyazuju.com/2026/09/16/578c41b3-a3e0-47d5-8498-23c084d76258.png', '#2979FF', '#EAF2FF', '/mobile-design/hotel/list', 3, 1, NULL, '2026-09-10 09:35:42', '2026-09-10 09:35:42');
INSERT INTO `tb_home_module` VALUES (4, 'grid', 'food_map', '美食地图', 'https://tu.mianyazuju.com/2026/09/16/48b127c9-6b1f-4227-b7e3-295113f49d38.png', '#FF6B8A', '#FFE8EE', '/mobile-design/map/index?type=food', 4, 1, NULL, '2026-09-10 09:35:42', '2026-09-10 09:35:42');
INSERT INTO `tb_home_module` VALUES (5, 'grid', 'parking', '停车通行', 'https://tu.mianyazuju.com/2026/09/16/5176dede-051e-410a-b854-fcbeadefbb81.png', '#FFB800', '#FFF8E6', '/mobile-design/map/index?type=parking', 5, 1, NULL, '2026-09-10 09:35:42', '2026-09-10 09:35:42');
INSERT INTO `tb_home_module` VALUES (6, 'grid', 'activity', '精彩活动', 'https://tu.mianyazuju.com/2026/09/16/3131cc92-2a26-46e0-ba1d-7a4f0cbcdf93.png', '#9C27B0', '#F3E8F8', '/mobile-design/activity/list', 6, 1, NULL, '2026-09-10 09:35:42', '2026-09-10 09:35:42');
INSERT INTO `tb_home_module` VALUES (7, 'grid', 'heritage', '非遗文化', 'https://tu.mianyazuju.com/2026/09/16/b951bb4f-14e3-480d-b723-297361a4cd1c.png', '#5D4037', '#EFEBE9', '/mobile-design/heritage/index', 7, 1, NULL, '2026-09-10 09:35:42', '2026-09-10 09:35:42');
INSERT INTO `tb_home_module` VALUES (8, 'grid', 'navigation', '地图导航', 'https://tu.mianyazuju.com/2026/09/16/5c898935-9715-4ef0-8d28-7c30a41b1022.png', '#00BCD4', '#E0F7FA', '/mobile-design/map/index?type=navigation', 8, 1, NULL, '2026-09-10 09:35:42', '2026-09-10 09:35:42');
INSERT INTO `tb_home_module` VALUES (9, 'grid', 'transport', '公共交通', 'https://tu.mianyazuju.com/2026/09/16/d8842845-cd95-4cc1-ab8c-fd6f628a9296.png', '#9575CD', '#EDE7F6', '/mobile-design/transport/list', 9, 1, NULL, '2026-09-10 09:35:42', '2026-09-10 09:35:42');
INSERT INTO `tb_home_module` VALUES (10, 'grid', 'route', '旅游线路', 'https://tu.mianyazuju.com/2026/09/16/52538edb-ab02-475d-b833-7b4d066e485d.png', '#FF6600', '#FFF3EB', '/mobile-design/route/list', 10, 1, NULL, '2026-09-10 09:35:42', '2026-09-10 09:35:42');
INSERT INTO `tb_home_module` VALUES (11, 'tab', 'recommend', '综合推荐', NULL, NULL, NULL, NULL, 0, 1, NULL, '2026-09-10 09:35:42', '2026-09-10 09:35:42');
INSERT INTO `tb_home_module` VALUES (12, 'tab', 'hotel', '酒店住宿', NULL, NULL, NULL, '/mobile-design/hotel/list', 1, 1, NULL, '2026-09-10 09:35:42', '2026-09-16 10:15:03');
INSERT INTO `tb_home_module` VALUES (13, 'tab', 'route', '旅游线路', NULL, NULL, NULL, '/mobile-design/route/list', 2, 1, NULL, '2026-09-10 09:35:42', '2026-09-16 10:14:49');
INSERT INTO `tb_home_module` VALUES (14, 'tab', 'activity', '精彩活动', NULL, NULL, NULL, '/mobile-design/activity/list', 3, 1, NULL, '2026-09-10 09:35:42', '2026-09-24 16:13:52');
INSERT INTO `tb_home_module` VALUES (15, 'tab', 'scenic', '旅游景区', NULL, NULL, NULL, '/mobile-design/scenic/list', 5, 1, NULL, '2026-09-10 09:35:42', '2026-09-24 16:22:33');
INSERT INTO `tb_home_module` VALUES (17, 'tab', 'heritage', '非遗好物', NULL, NULL, NULL, '/mobile-design/heritage/index', 4, 0, NULL, '2026-09-24 16:22:33', '2026-09-24 16:35:47');

-- ----------------------------
-- Table structure for tb_hotel
-- ----------------------------
DROP TABLE IF EXISTS `tb_hotel`;
CREATE TABLE `tb_hotel`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id` bigint(0) NOT NULL COMMENT '商户ID',
  `destination_id` bigint(0) NOT NULL COMMENT '目的地ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '酒店名称',
  `category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类：luxury/boutique/business/resort',
  `star_level` tinyint(0) NULL DEFAULT NULL COMMENT '星级',
  `star_label` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '星级文案（商户自定义）',
  `address` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地址',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `score` decimal(3, 1) NULL DEFAULT NULL COMMENT '评分',
  `tag` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签',
  `price_from` decimal(12, 2) NULL DEFAULT NULL COMMENT '起步价',
  `cover_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图',
  `banner_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '轮播图JSON',
  `check_in_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '入住时间说明',
  `check_out_time` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '退房时间说明',
  `facilities` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '设施JSON',
  `intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '简介',
  `policy_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '??????JSON',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_hotel_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_hotel_destination_id`(`destination_id`) USING BTREE,
  INDEX `idx_hotel_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '酒店' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_hotel
-- ----------------------------
INSERT INTO `tb_hotel` VALUES (5, 5, 4, '石榴大酒店', 'all', 4, '四星级', '奥尊游泳朗陈20楼', 34.211230, 108.881189, '18892020826', NULL, NULL, 128.00, 'https://tu.mianyazuju.com/2026/09/17/fa8eb20e-0e14-4847-a490-18c94a8fa8fd.jpg', '[\"https://tu.mianyazuju.com/2026/09/17/e72b3857-e276-4ad1-94f9-ad6a8b467d47.jpg\",\"https://tu.mianyazuju.com/2026/09/17/c7c42056-8e75-4a23-9b97-534f14077585.jpeg\",\"https://tu.mianyazuju.com/2026/09/17/090cd7e9-0f32-448f-82b4-db9e1bd36c09.jpg\"]', '14:00 后入住', '12:00 前退房', '[{\"id\":1,\"icon\":\"wifi\",\"label\":\"免费WiFi\"},{\"id\":2,\"icon\":\"car-fill\",\"label\":\"免费停车\"},{\"id\":3,\"icon\":\"grid-fill\",\"label\":\"健身房\"},{\"id\":4,\"icon\":\"gift-fill\",\"label\":\"含早餐\"}]', '酒店位于环境怡人、风景秀丽、旅游资源丰富、历史文化浓郁的的新县大别山腹地，是一座集度假、会议、住宿、餐饮、休闲娱乐多功能为一体的高端度假酒店。作为接待中央及省、市领导的政商名流云集之地，是您迎春踏青、消夏避暑、金秋赏月、冬觅清幽的上佳之选。酒店总建筑面积达 112000 m2、拥有豪华客房多间、属信阳地区设施最完备豪华酒店度假群，是各类会议、商旅团队及度假游客的优选之地。', '{\"bookingNotice\":\"订单需等酒店或供应商确认后生效，确认结果以短信、邮件或 App 通知为准。\",\"checkInNotice\":\"前台 24 小时服务，请于入住时在柜台办理登记。\",\"depositPolicy\":\"不收取押金\",\"breakfastInfo\":\"类型：自助餐 菜品：中式 营业时间：周一至周日07:00-09:00开放\"}', 1, 0, '2026-09-16 20:12:20', '2026-09-17 16:56:57');

-- ----------------------------
-- Table structure for tb_hotel_review
-- ----------------------------
DROP TABLE IF EXISTS `tb_hotel_review`;
CREATE TABLE `tb_hotel_review`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `hotel_id` bigint(0) NOT NULL COMMENT '酒店ID',
  `order_id` bigint(0) NOT NULL COMMENT '关联订单ID',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '评价用户展示名',
  `content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评价内容',
  `rating` tinyint(0) NOT NULL DEFAULT 5 COMMENT '评分1-5',
  `images` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '评价图片URL列表(JSON)',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_hotel_review_order_user`(`order_id`, `user_id`) USING BTREE,
  INDEX `idx_hotel_review_hotel_id`(`hotel_id`) USING BTREE,
  INDEX `idx_hotel_review_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '酒店用户评价' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_hotel_review
-- ----------------------------
INSERT INTO `tb_hotel_review` VALUES (1, 5, 15, 2, '18892020826', '很好', 5, '[\"https://tu.mianyazuju.com/2026/09/20/6a2ce44b-3925-4bef-a9d8-be3be127cbec.webp\",\"https://tu.mianyazuju.com/2026/09/20/0d8a2429-990a-4e67-8dd4-bc3b74831941.webp\"]', '2026-09-20 11:07:56');

-- ----------------------------
-- Table structure for tb_hotel_room
-- ----------------------------
DROP TABLE IF EXISTS `tb_hotel_room`;
CREATE TABLE `tb_hotel_room`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `hotel_id` bigint(0) NOT NULL COMMENT '酒店ID',
  `merchant_id` bigint(0) NOT NULL COMMENT '商户ID',
  `merchant_service_id` bigint(0) NULL DEFAULT NULL COMMENT '关联商户服务ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '房型名称',
  `spec` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规格描述',
  `price` decimal(12, 2) NOT NULL COMMENT '每晚价格',
  `stock` int(0) NOT NULL DEFAULT 0 COMMENT '库存',
  `cover_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图',
  `room_detail_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '房型扩展 JSON',
  `room_standard_id` bigint(0) NULL DEFAULT NULL COMMENT '关联客房标准ID',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1在售 0下架',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_hotel_room_merchant_service`(`merchant_service_id`) USING BTREE,
  INDEX `idx_hotel_room_hotel_id`(`hotel_id`) USING BTREE,
  INDEX `idx_hotel_room_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_hotel_room_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '酒店房型' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_hotel_room
-- ----------------------------
INSERT INTO `tb_hotel_room` VALUES (10, 5, 5, 13, '标准大床房', '30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物', 128.00, 10, 'https://tu.mianyazuju.com/2026/09/17/7d4bb40c-2520-48c2-994a-3bf84e005a55.png', '{\"inheritStandard\":true,\"area\":\"30\",\"floor\":\"5、6\",\"bedType\":\"大床\",\"bedSize\":\"1.8\",\"extraBedPolicy\":\"不可加床\",\"occupancy\":2,\"bedCount\":1,\"gallery\":[],\"tags\":[],\"facilities\":[]}', 2, 1, 0, '2026-09-17 16:26:09', '2026-09-17 16:26:09');

-- ----------------------------
-- Table structure for tb_hotel_room_calendar
-- ----------------------------
DROP TABLE IF EXISTS `tb_hotel_room_calendar`;
CREATE TABLE `tb_hotel_room_calendar`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `room_id` bigint(0) NOT NULL COMMENT '房型ID',
  `hotel_id` bigint(0) NOT NULL COMMENT '酒店ID',
  `merchant_id` bigint(0) NOT NULL COMMENT '商户ID',
  `calendar_date` date NOT NULL COMMENT '日期',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '当日价格，空则沿用房型基础价',
  `stock` int(0) NULL DEFAULT NULL COMMENT '当日可售间数，空则沿用房型库存',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '1开放 0关房',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_room_date`(`room_id`, `calendar_date`) USING BTREE,
  INDEX `idx_hotel_date`(`hotel_id`, `calendar_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '酒店房型日历房态' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_hotel_room_calendar
-- ----------------------------
INSERT INTO `tb_hotel_room_calendar` VALUES (1, 10, 5, 5, '2026-09-25', 128.00, 5, 1, '2026-09-18 12:05:35', '2026-09-18 12:05:34');

-- ----------------------------
-- Table structure for tb_hotel_room_standard
-- ----------------------------
DROP TABLE IF EXISTS `tb_hotel_room_standard`;
CREATE TABLE `tb_hotel_room_standard`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `hotel_id` bigint(0) NOT NULL COMMENT '酒店ID',
  `merchant_id` bigint(0) NOT NULL COMMENT '商户ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板名称',
  `floor_hint` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '适用楼层备注',
  `facilities_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '房间设施 JSON',
  `defaults_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '默认参数 JSON',
  `gallery_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '共用相册 JSON',
  `tags_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '共用标签 JSON',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_hotel_room_standard_hotel`(`hotel_id`) USING BTREE,
  INDEX `idx_hotel_room_standard_merchant`(`merchant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '酒店客房标准模板' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_hotel_room_standard
-- ----------------------------
INSERT INTO `tb_hotel_room_standard` VALUES (2, 5, 5, '标准大床房', '5、6', '[{\"id\":1,\"icon\":\"wifi\",\"label\":\"免费WiFi\"},{\"id\":2,\"icon\":\"gift-fill\",\"label\":\"含早餐\"},{\"id\":3,\"icon\":\"bag-fill\",\"label\":\"洗衣服务\"}]', '{\"windowType\":\"部分有窗\",\"smokingPolicy\":\"可吸烟\",\"petPolicy\":\"不可携带宠物\"}', '[\"https://tu.mianyazuju.com/2026/09/17/681575ae-8286-43b9-b5a9-6f93d34cb2aa.png\",\"https://tu.mianyazuju.com/2026/09/17/e408e7ff-82ef-4980-9ad3-8cbb8c736a20.webp\",\"https://tu.mianyazuju.com/2026/09/17/9f19fca6-2852-4c6c-97c5-00df512b6ef5.webp\",\"https://tu.mianyazuju.com/2026/09/17/62178dbd-9ac4-4835-ac15-a69c14d62d1f.webp\"]', '[\"延迟退房\",\"安心消毒\",\"近景区\"]', 0, 1, '2026-09-17 14:01:50', '2026-09-17 14:01:49');
INSERT INTO `tb_hotel_room_standard` VALUES (3, 5, 5, '标准双床房', '1-3', '[{\"id\":1,\"icon\":\"wifi\",\"label\":\"免费WiFi\"},{\"id\":2,\"icon\":\"grid-fill\",\"label\":\"健身房\"},{\"id\":3,\"icon\":\"gift-fill\",\"label\":\"含早餐\"}]', '{\"windowType\":\"部分有窗\",\"smokingPolicy\":\"可吸烟\",\"petPolicy\":\"宠物友好\"}', '[\"https://tu.mianyazuju.com/2026/09/17/81855d4f-ecbf-4418-bb7b-3642ff1236cc.png\",\"https://tu.mianyazuju.com/2026/09/17/2fc43ab1-3c40-4339-918b-a0910a64beac.webp\",\"https://tu.mianyazuju.com/2026/09/17/b35acd47-30bf-4446-a952-7de4d88b05f3.png\"]', '[\"免费停车\",\"延迟退房\",\"安心消毒\"]', 0, 1, '2026-09-17 14:16:43', '2026-09-17 14:16:43');

-- ----------------------------
-- Table structure for tb_invite_relation
-- ----------------------------
DROP TABLE IF EXISTS `tb_invite_relation`;
CREATE TABLE `tb_invite_relation`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) NOT NULL COMMENT '被邀请用户ID',
  `inviter_user_id` bigint(0) NULL DEFAULT NULL COMMENT '一级邀请人用户ID',
  `inviter_level2_user_id` bigint(0) NULL DEFAULT NULL COMMENT '二级邀请人用户ID',
  `invite_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '注册时填写的邀请码',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '绑定时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_invite_user_id`(`user_id`) USING BTREE,
  INDEX `idx_invite_inviter_user_id`(`inviter_user_id`) USING BTREE,
  INDEX `idx_invite_inviter_level2_user_id`(`inviter_level2_user_id`) USING BTREE,
  INDEX `idx_invite_code`(`invite_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '邀请关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_invite_relation
-- ----------------------------
INSERT INTO `tb_invite_relation` VALUES (1, 2, 1, NULL, 'HUIPVG', '2026-09-14 14:03:26', '2026-09-14 14:03:25');
INSERT INTO `tb_invite_relation` VALUES (2, 3, 2, 1, 'IUIPVG', '2026-09-21 13:46:12', '2026-09-21 13:46:11');
INSERT INTO `tb_invite_relation` VALUES (3, 4, 2, 1, 'IUIPVG', '2026-09-22 10:38:51', '2026-09-22 10:38:51');

-- ----------------------------
-- Table structure for tb_kefu_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_kefu_message`;
CREATE TABLE `tb_kefu_message`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id` bigint(0) NOT NULL COMMENT '会话ID',
  `sender_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发送方：user/staff',
  `sender_id` bigint(0) NULL DEFAULT NULL COMMENT '发送人ID（用户或管理员）',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息内容',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kefu_message_session`(`session_id`) USING BTREE,
  INDEX `idx_kefu_message_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '在线客服消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_kefu_message
-- ----------------------------
INSERT INTO `tb_kefu_message` VALUES (1, 1, 'user', 1, '你好', '2026-09-15 10:21:12');
INSERT INTO `tb_kefu_message` VALUES (2, 1, 'staff', 1, '你好请问有什么问题', '2026-09-15 10:35:18');
INSERT INTO `tb_kefu_message` VALUES (3, 1, 'user', 1, '阿斯顿', '2026-09-15 10:35:26');
INSERT INTO `tb_kefu_message` VALUES (4, 1, 'staff', 1, '但是', '2026-09-15 10:35:32');
INSERT INTO `tb_kefu_message` VALUES (5, 3, 'user', 5, '你好', '2026-09-23 20:36:38');
INSERT INTO `tb_kefu_message` VALUES (6, 3, 'staff', 1, '你好请问想要咨询什么', '2026-09-23 20:36:56');

-- ----------------------------
-- Table structure for tb_kefu_session
-- ----------------------------
DROP TABLE IF EXISTS `tb_kefu_session`;
CREATE TABLE `tb_kefu_session`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) NOT NULL COMMENT 'C端用户ID',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'open' COMMENT '会话状态：open/closed',
  `last_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后一条消息摘要',
  `last_message_time` datetime(0) NULL DEFAULT NULL COMMENT '最后消息时间',
  `admin_unread_count` int(0) NOT NULL DEFAULT 0 COMMENT '客服未读数',
  `user_unread_count` int(0) NOT NULL DEFAULT 0 COMMENT '用户未读数',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kefu_session_user`(`user_id`) USING BTREE,
  INDEX `idx_kefu_session_status`(`status`) USING BTREE,
  INDEX `idx_kefu_session_last_time`(`last_message_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '在线客服会话' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_kefu_session
-- ----------------------------
INSERT INTO `tb_kefu_session` VALUES (1, 1, 'open', '但是', '2026-09-15 10:35:32', 0, 0, '2026-09-15 10:21:12', '2026-09-15 10:52:19');
INSERT INTO `tb_kefu_session` VALUES (2, 2, 'open', NULL, NULL, 0, 0, '2026-09-16 10:29:05', '2026-09-16 10:29:05');
INSERT INTO `tb_kefu_session` VALUES (3, 5, 'open', '你好请问想要咨询什么', '2026-09-23 20:36:56', 0, 0, '2026-09-23 20:36:26', '2026-09-23 20:36:56');

-- ----------------------------
-- Table structure for tb_map_poi
-- ----------------------------
DROP TABLE IF EXISTS `tb_map_poi`;
CREATE TABLE `tb_map_poi`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `destination_id` bigint(0) NOT NULL COMMENT '目的地ID',
  `poi_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'POI类型：parking/transport',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `address` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地址',
  `latitude` decimal(10, 7) NOT NULL COMMENT '纬度',
  `longitude` decimal(10, 7) NOT NULL COMMENT '经度',
  `tag` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签',
  `spaces` int(0) NULL DEFAULT NULL COMMENT '剩余车位（停车类）',
  `total_spaces` int(0) NULL DEFAULT NULL COMMENT '总车位（停车类）',
  `fee_desc` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收费说明',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_map_poi_destination`(`destination_id`) USING BTREE,
  INDEX `idx_map_poi_type`(`poi_type`) USING BTREE,
  INDEX `idx_map_poi_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '地图POI（停车/交通）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_map_poi
-- ----------------------------
INSERT INTO `tb_map_poi` VALUES (1, 1, 'parking', '万灵古镇停车场', '荣昌区万灵镇入口', 29.4035000, 105.5812000, '景区停车', 86, 120, '5元/小时', 1, 1, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_map_poi` VALUES (2, 1, 'parking', '安陶小镇停车场', '荣昌区安富街道安陶路', 29.3978000, 105.6035000, '景区停车', 42, 80, '免费2小时', 1, 2, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_map_poi` VALUES (3, 1, 'parking', '荣昌北站P2停车场', '荣昌区荣昌北站', 29.4215000, 105.5988000, '交通枢纽', 130, 200, '3元/小时', 1, 3, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_map_poi` VALUES (4, 1, 'transport', '荣昌北站', '荣昌区荣昌北站', 29.4220000, 105.5995000, '高铁站', NULL, NULL, NULL, 1, 1, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_map_poi` VALUES (5, 1, 'transport', '荣昌客运中心', '荣昌区昌州大道东段', 29.4150000, 105.6050000, '长途客运', NULL, NULL, NULL, 1, 2, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_map_poi` VALUES (6, 4, 'parking', '秦始皇帝陵博物院停车场', '临潼区秦陵北路兵马俑景区', 34.3825000, 109.2758000, '景区停车', NULL, NULL, '按景区公示收费', 1, 1, '2026-09-10 09:44:16', '2026-09-10 09:44:16');
INSERT INTO `tb_map_poi` VALUES (7, 4, 'parking', '大雁塔北广场地下停车场', '雁塔区慈恩路大雁塔北广场', 34.2205000, 108.9628000, '景区停车', NULL, NULL, '按小时计费', 1, 2, '2026-09-10 09:44:16', '2026-09-10 09:44:16');
INSERT INTO `tb_map_poi` VALUES (8, 4, 'transport', '西安北站', '未央区元朔路', 34.3772220, 108.9397220, '高铁站', NULL, NULL, NULL, 1, 1, '2026-09-10 09:44:16', '2026-09-10 09:44:16');
INSERT INTO `tb_map_poi` VALUES (9, 4, 'transport', '西安咸阳国际机场', '咸阳市渭城区空港新城', 34.4471190, 108.7515920, '机场', NULL, NULL, NULL, 1, 2, '2026-09-10 09:44:16', '2026-09-10 09:44:16');
INSERT INTO `tb_map_poi` VALUES (10, 4, 'transport', '地铁9号线华清池站', '临潼区华清路与西关正街交叉口', 34.3628000, 109.2135000, '地铁站', NULL, NULL, NULL, 1, 3, '2026-09-10 09:44:16', '2026-09-10 09:44:16');

-- ----------------------------
-- Table structure for tb_merchant
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant`;
CREATE TABLE `tb_merchant`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户业务编码',
  `user_id` bigint(0) NOT NULL COMMENT '关联用户ID（tb_user.user_id）',
  `destination_id` bigint(0) NOT NULL COMMENT '所属目的地ID',
  `store_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '门店名称',
  `store_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '门店类型：hotel/restaurant/travel/culture',
  `shop_category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '闈為仐濂藉簵鍒嗙被锛堟枃鍒涘晢鎴凤級',
  `craft_id` bigint(0) NULL DEFAULT NULL COMMENT '关联非遗工艺ID（文创商户）',
  `contact_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `store_region` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '门店地区',
  `store_address` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '门店详细地址',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度',
  `store_hours` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业时间',
  `license_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '统一社会信用代码',
  `license_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业执照图片URL',
  `legal_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '法人姓名',
  `legal_id_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '法人身份证号',
  `legal_id_front` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '法人身份证正面URL',
  `legal_id_back` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '法人身份证反面URL',
  `qual_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '行业资质图片JSON',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1正常 2禁用 0待激活',
  `is_open` tinyint(0) NOT NULL DEFAULT 1 COMMENT '营业状态：1营业中 0休息中',
  `queue_enabled` tinyint(0) NOT NULL DEFAULT 0 COMMENT 'queue enabled',
  `queue_table_count` int(0) NOT NULL DEFAULT 0 COMMENT 'queue table count',
  `home_recommend_enabled` tinyint(0) NOT NULL DEFAULT 0 COMMENT '首页推荐：1开启 0关闭',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_merchant_code`(`merchant_code`) USING BTREE,
  INDEX `idx_merchant_destination_id`(`destination_id`) USING BTREE,
  INDEX `idx_merchant_status`(`status`) USING BTREE,
  INDEX `idx_merchant_user_id`(`user_id`) USING BTREE,
  INDEX `idx_merchant_home_recommend`(`home_recommend_enabled`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_merchant
-- ----------------------------
INSERT INTO `tb_merchant` VALUES (5, 'MC202609160958199398', 2, 4, '石榴大酒店', 'hotel', NULL, NULL, '十六', '18892020826', '奥尊游泳', '奥尊游泳朗陈20楼', 34.2112300, 108.8811890, '00:00-23:59', '十六', 'https://tu.mianyazuju.com/2026/09/15/0f5fb73b-7d3e-474a-854f-73046837dc4f.png', '十六', '610526199902215252', 'https://tu.mianyazuju.com/2026/09/15/5d494849-e317-45bf-93b3-0ad627213f95.png', 'https://tu.mianyazuju.com/2026/09/15/45f96631-7f00-49eb-b52e-43ae861d2d80.png', '{\"storeFront\":\"https://tu.mianyazuju.com/2026/09/15/4bf77b32-7f3c-4238-9fbe-bf03112ed2fe.webp\"}', 1, 1, 0, 0, 0, '2026-09-16 09:58:19', '2026-09-21 14:53:50');
INSERT INTO `tb_merchant` VALUES (6, 'MC202609211427189136', 3, 4, '十七旅行社', 'travel', NULL, NULL, '十七', '18892020827', '陕西省西安市新城区', '陕西省西安市新城区关中巷十七旅行社', 34.2648360, 108.9701210, '09:00-21:00', 'SD123652', 'https://tu.mianyazuju.com/2026/09/21/58743789-bf98-4bc5-9d3b-455239052834.webp', '十七', '162521252145695452', 'https://tu.mianyazuju.com/2026/09/21/810338d3-c829-4fbc-9238-21e982ff6b67.jpg', 'https://tu.mianyazuju.com/2026/09/21/cdc1f6ec-a8e1-4396-9f07-5aecd2f6f562.webp', '{\"officePhoto\":\"https://tu.mianyazuju.com/2026/09/21/e178b684-e9b0-44da-9ae1-6a4e72c5d7dd.jpg\"}', 1, 1, 0, 0, 0, '2026-09-21 14:27:19', '2026-09-21 14:31:25');
INSERT INTO `tb_merchant` VALUES (7, 'MC202609221140475047', 4, 4, '十八陕菜馆', 'restaurant', NULL, NULL, '十八', '18892020828', '陕西省西安市雁塔区', '陕西省西安市雁塔区慈恩路十八陕菜馆', 34.2182210, 108.9641100, '09:00-21:00', 'DM1234563', 'https://tu.mianyazuju.com/2026/09/22/253745a8-18be-4aca-84f6-369e15d46d37.jpg', '十八', '152632512525255', 'https://tu.mianyazuju.com/2026/09/22/37f3d1c9-6c9e-4fb9-9c79-1e805cb4749d.webp', 'https://tu.mianyazuju.com/2026/09/22/f0c35672-ba0f-46eb-923a-3e6b5987aaeb.jpg', '{\"foodPermit\":\"https://tu.mianyazuju.com/2026/09/22/989f66c6-8917-4e99-b190-7691ec1d5860.png\",\"storeFront\":\"https://tu.mianyazuju.com/2026/09/22/b2065cdc-05dd-416f-a5a3-0f113282df0e.webp\",\"diningEnv\":\"[\\\"https://tu.mianyazuju.com/2026/09/22/b6e34daa-0386-4622-b9d2-796dbc6b7933.webp\\\",\\\"https://tu.mianyazuju.com/2026/09/22/af38b1e7-d503-4162-abea-02606cd93321.jpg\\\",\\\"https://tu.mianyazuju.com/2026/09/22/a8dc47e0-8bdf-45fd-a359-6573550a069b.webp\\\"]\"}', 1, 1, 1, 7, 0, '2026-09-22 11:40:48', '2026-09-23 14:00:01');
INSERT INTO `tb_merchant` VALUES (8, 'MC202609231542549843', 5, 4, '叁拾文创专卖店', 'culture', 'textile', NULL, '叁拾', '18892020830', '陕西省西安市雁塔区', '陕西省西安市雁塔区科技六路叁拾文创专卖店', 34.2103190, 108.8723910, '09:00-21:00', 'DM5214528545', 'https://tu.mianyazuju.com/2026/09/23/0bac6c97-1d3d-4c79-9336-3d5e99c75bb7.png', '叁拾', '610526199525102521', 'https://tu.mianyazuju.com/2026/09/23/ff18bb23-1081-48b9-9d1e-8c2504c04d01.jpg', 'https://tu.mianyazuju.com/2026/09/23/b60b40bf-27fb-4b4e-9c2e-e8cf0b500a44.jpg', '{\"storeFront\":\"https://tu.mianyazuju.com/2026/09/23/0cc28f4e-b0fd-4809-8d32-a2a5b262b5a1.webp\"}', 1, 1, 0, 0, 0, '2026-09-23 15:42:54', '2026-09-24 09:53:26');

-- ----------------------------
-- Table structure for tb_merchant_apply
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_apply`;
CREATE TABLE `tb_merchant_apply`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `apply_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '申请单号',
  `user_id` bigint(0) NOT NULL COMMENT '申请人用户ID',
  `destination_id` bigint(0) NULL DEFAULT NULL COMMENT '意向目的地ID',
  `store_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '门店名称',
  `store_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '门店类型',
  `shop_category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '闈為仐濂藉簵鍒嗙被锛堟枃鍒涘晢鎴峰繀濉?級',
  `craft_id` bigint(0) NULL DEFAULT NULL COMMENT '关联非遗工艺ID（文创商户必填）',
  `contact_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系电话',
  `store_region` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '门店地区',
  `store_address` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '门店详细地址',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度（地图选点）',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度（地图选点）',
  `store_hours` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业时间',
  `license_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '统一社会信用代码',
  `license_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '营业执照图片URL',
  `legal_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '法人姓名',
  `legal_id_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '法人身份证号',
  `legal_id_front` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '法人身份证正面URL',
  `legal_id_back` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '法人身份证反面URL',
  `qual_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '行业资质图片JSON',
  `audit_status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核 1通过 2驳回',
  `audit_remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核备注',
  `merchant_id` bigint(0) NULL DEFAULT NULL COMMENT '审核通过后关联商户ID',
  `submitted_at` datetime(0) NULL DEFAULT NULL COMMENT '提交时间',
  `audited_at` datetime(0) NULL DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_merchant_apply_no`(`apply_no`) USING BTREE,
  INDEX `idx_merchant_apply_user_id`(`user_id`) USING BTREE,
  INDEX `idx_merchant_apply_destination_id`(`destination_id`) USING BTREE,
  INDEX `idx_merchant_apply_status`(`audit_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户入驻申请' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_merchant_apply
-- ----------------------------
INSERT INTO `tb_merchant_apply` VALUES (1, 'MA202609151811478216', 2, 4, '石榴大酒店', 'hotel', NULL, NULL, '十六', '18892020826', '奥尊游泳', '奥尊游泳朗陈20楼', 34.2112300, 108.8811890, '00:00-23:59', '十六', 'https://tu.mianyazuju.com/2026/09/15/0f5fb73b-7d3e-474a-854f-73046837dc4f.png', '十六', '610526199902215252', 'https://tu.mianyazuju.com/2026/09/15/5d494849-e317-45bf-93b3-0ad627213f95.png', 'https://tu.mianyazuju.com/2026/09/15/45f96631-7f00-49eb-b52e-43ae861d2d80.png', '{\"storeFront\":\"https://tu.mianyazuju.com/2026/09/15/4bf77b32-7f3c-4238-9fbe-bf03112ed2fe.webp\"}', 1, NULL, 5, '2026-09-16 09:58:09', '2026-09-16 09:58:19', '2026-09-15 18:11:47', '2026-09-16 09:58:19');
INSERT INTO `tb_merchant_apply` VALUES (2, 'MA202609211424276867', 3, 4, '十七旅行社', 'travel', NULL, NULL, '十七', '18892020827', '陕西省西安市新城区', '陕西省西安市新城区关中巷十七旅行社', 34.2648360, 108.9701210, '09:00-21:00', 'SD123652', 'https://tu.mianyazuju.com/2026/09/21/58743789-bf98-4bc5-9d3b-455239052834.webp', '十七', '162521252145695452', 'https://tu.mianyazuju.com/2026/09/21/810338d3-c829-4fbc-9238-21e982ff6b67.jpg', 'https://tu.mianyazuju.com/2026/09/21/cdc1f6ec-a8e1-4396-9f07-5aecd2f6f562.webp', '{\"officePhoto\":\"https://tu.mianyazuju.com/2026/09/21/e178b684-e9b0-44da-9ae1-6a4e72c5d7dd.jpg\"}', 1, NULL, 6, '2026-09-21 14:25:32', '2026-09-21 14:27:19', '2026-09-21 14:24:28', '2026-09-21 14:27:19');
INSERT INTO `tb_merchant_apply` VALUES (3, 'MA202609221107346425', 4, 4, '十八陕菜馆', 'restaurant', NULL, NULL, '十八', '18892020828', '陕西省西安市雁塔区', '陕西省西安市雁塔区慈恩路十八陕菜馆', 34.2182210, 108.9641100, '09:00-21:00', 'DM1234563', 'https://tu.mianyazuju.com/2026/09/22/253745a8-18be-4aca-84f6-369e15d46d37.jpg', '十八', '152632512525255', 'https://tu.mianyazuju.com/2026/09/22/37f3d1c9-6c9e-4fb9-9c79-1e805cb4749d.webp', 'https://tu.mianyazuju.com/2026/09/22/f0c35672-ba0f-46eb-923a-3e6b5987aaeb.jpg', '{\"foodPermit\":\"https://tu.mianyazuju.com/2026/09/22/989f66c6-8917-4e99-b190-7691ec1d5860.png\",\"storeFront\":\"https://tu.mianyazuju.com/2026/09/22/b2065cdc-05dd-416f-a5a3-0f113282df0e.webp\",\"diningEnv\":\"[\\\"https://tu.mianyazuju.com/2026/09/22/b6e34daa-0386-4622-b9d2-796dbc6b7933.webp\\\",\\\"https://tu.mianyazuju.com/2026/09/22/af38b1e7-d503-4162-abea-02606cd93321.jpg\\\",\\\"https://tu.mianyazuju.com/2026/09/22/a8dc47e0-8bdf-45fd-a359-6573550a069b.webp\\\"]\"}', 1, NULL, 7, '2026-09-22 11:26:27', '2026-09-22 11:40:48', '2026-09-22 11:07:34', '2026-09-22 11:40:48');
INSERT INTO `tb_merchant_apply` VALUES (4, 'MA202609231542249173', 5, 4, '叁拾文创专卖店', 'culture', 'textile', NULL, '叁拾', '18892020830', '陕西省西安市雁塔区', '陕西省西安市雁塔区科技六路叁拾文创专卖店', 34.2103190, 108.8723910, '09:00-21:00', 'DM5214528545', 'https://tu.mianyazuju.com/2026/09/23/0bac6c97-1d3d-4c79-9336-3d5e99c75bb7.png', '叁拾', '610526199525102521', 'https://tu.mianyazuju.com/2026/09/23/ff18bb23-1081-48b9-9d1e-8c2504c04d01.jpg', 'https://tu.mianyazuju.com/2026/09/23/b60b40bf-27fb-4b4e-9c2e-e8cf0b500a44.jpg', '{\"storeFront\":\"https://tu.mianyazuju.com/2026/09/23/0cc28f4e-b0fd-4809-8d32-a2a5b262b5a1.webp\"}', 1, NULL, 8, '2026-09-24 11:00:35', '2026-09-24 11:00:44', '2026-09-23 15:42:24', '2026-09-24 11:00:44');

-- ----------------------------
-- Table structure for tb_merchant_deposit_pay
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_deposit_pay`;
CREATE TABLE `tb_merchant_deposit_pay`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pay_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '保证金支付单号',
  `merchant_id` bigint(0) NOT NULL COMMENT '商户ID',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `amount` decimal(12, 2) NOT NULL COMMENT '支付金额',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态：0待支付 1已支付 2已取消',
  `pay_method` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付方式',
  `pay_time` datetime(0) NULL DEFAULT NULL COMMENT '支付时间',
  `trade_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '第三方交易号',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_merchant_deposit_pay_no`(`pay_no`) USING BTREE,
  INDEX `idx_merchant_deposit_pay_merchant`(`merchant_id`) USING BTREE,
  INDEX `idx_merchant_deposit_pay_user`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户保证金支付单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_merchant_deposit_pay
-- ----------------------------
INSERT INTO `tb_merchant_deposit_pay` VALUES (1, 'MD202609201029571692', 5, 2, 5000.00, 0, NULL, NULL, NULL, '2026-09-20 10:29:57', '2026-09-20 10:29:57');
INSERT INTO `tb_merchant_deposit_pay` VALUES (2, 'MD202609201030001773', 5, 2, 5000.00, 0, NULL, NULL, NULL, '2026-09-20 10:30:01', '2026-09-20 10:30:00');
INSERT INTO `tb_merchant_deposit_pay` VALUES (3, 'MD202609201030404012', 5, 2, 5000.00, 0, NULL, NULL, NULL, '2026-09-20 10:30:41', '2026-09-20 10:30:40');
INSERT INTO `tb_merchant_deposit_pay` VALUES (4, 'MD202609201033395010', 5, 2, 5000.00, 1, 'wallet', '2026-09-20 10:34:45', NULL, '2026-09-20 10:33:40', '2026-09-20 10:34:44');
INSERT INTO `tb_merchant_deposit_pay` VALUES (5, 'MD202609211127282454', 5, 2, 1.00, 1, 'wallet', '2026-09-21 11:27:30', NULL, '2026-09-21 11:27:29', '2026-09-21 11:27:29');
INSERT INTO `tb_merchant_deposit_pay` VALUES (6, 'MD202609211430512064', 6, 3, 5.00, 1, 'wallet', '2026-09-21 14:30:52', NULL, '2026-09-21 14:30:51', '2026-09-21 14:30:52');
INSERT INTO `tb_merchant_deposit_pay` VALUES (7, 'MD202609221310017630', 7, 4, 3000.00, 1, 'wallet', '2026-09-22 13:10:04', NULL, '2026-09-22 13:10:01', '2026-09-22 13:10:04');
INSERT INTO `tb_merchant_deposit_pay` VALUES (8, 'MD202609231543439433', 8, 5, 2000.00, 1, 'wallet', '2026-09-23 15:43:44', NULL, '2026-09-23 15:43:43', '2026-09-23 15:43:44');

-- ----------------------------
-- Table structure for tb_merchant_deposit_refund
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_deposit_refund`;
CREATE TABLE `tb_merchant_deposit_refund`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `refund_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '退保申请单号',
  `merchant_id` bigint(0) NOT NULL COMMENT '商户ID',
  `user_id` bigint(0) NOT NULL COMMENT '申请人用户ID',
  `amount` decimal(12, 2) NOT NULL COMMENT '申请退还金额',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态：0待审核 1已通过 2已驳回',
  `apply_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '申请原因',
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核备注',
  `audit_time` datetime(0) NULL DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_merchant_deposit_refund_no`(`refund_no`) USING BTREE,
  INDEX `idx_merchant_deposit_refund_merchant`(`merchant_id`) USING BTREE,
  INDEX `idx_merchant_deposit_refund_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户保证金退还申请' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_merchant_deposit_refund
-- ----------------------------
INSERT INTO `tb_merchant_deposit_refund` VALUES (1, 'DR202609201055096403', 5, 2, 5000.00, 2, NULL, '的发射点士大夫', '2026-09-20 10:59:43', '2026-09-20 10:55:10', '2026-09-20 10:59:42');
INSERT INTO `tb_merchant_deposit_refund` VALUES (2, 'DR202609201353469086', 5, 2, 5000.00, 1, NULL, NULL, '2026-09-20 13:53:59', '2026-09-20 13:53:46', '2026-09-20 13:53:59');

-- ----------------------------
-- Table structure for tb_merchant_service
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_service`;
CREATE TABLE `tb_merchant_service`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id` bigint(0) NOT NULL COMMENT '商户ID',
  `service_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '服务业务编码',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '服务名称',
  `price` decimal(12, 2) NOT NULL COMMENT '服务价格，0表示免费预约',
  `stock` int(0) NOT NULL DEFAULT 0 COMMENT '库存/名额',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务说明',
  `spec` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规格描述（酒店房型规格等）',
  `meet_point` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '集合地点（旅游线路服务，优先于门店装修）',
  `spots` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '途经景点（逗号分隔，旅游线路服务）',
  `travel_city` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `day_count` int(0) NULL DEFAULT NULL COMMENT '????',
  `itinerary_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '????JSON???/??/???',
  `goods_specs` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '商品规格JSON（单规格为空，多规格含SKU列表）',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '详情介绍（文创商品等）',
  `room_detail_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '酒店房型扩展 JSON',
  `room_standard_id` bigint(0) NULL DEFAULT NULL COMMENT '关联客房标准ID',
  `cover_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `banner_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '?????SON???',
  `detail_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '商品详情图JSON数组（文创）',
  `monthly_sales` int(0) NOT NULL DEFAULT 0 COMMENT '月售数量（餐饮菜品）',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'draft' COMMENT '状态：on_sale/off_sale/draft',
  `home_tab_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '首页推荐Tab业务键，对应 tb_home_module.module_key',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序，升序',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_merchant_service_code`(`service_code`) USING BTREE,
  INDEX `idx_merchant_service_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_merchant_service_status`(`status`) USING BTREE,
  INDEX `idx_merchant_service_home_tab`(`home_tab_key`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户服务' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_merchant_service
-- ----------------------------
INSERT INTO `tb_merchant_service` VALUES (1, 2, 'seed_goose_001_dish_1', '招牌整鹅', 168.00, 1000, '整只白鹅卤制，皮脆肉嫩，附赠秘制蘸料。', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 520, 'on_sale', NULL, 1, '2026-09-10 09:44:08', '2026-09-24 15:26:25');
INSERT INTO `tb_merchant_service` VALUES (2, 2, 'seed_goose_001_dish_2', '经典半鹅', 68.00, 999, '半只装，适合2-3人分享，附赠卤汁。', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 890, 'on_sale', NULL, 2, '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_merchant_service` VALUES (3, 2, 'seed_goose_001_dish_3', '卤鹅翅', 38.00, 999, '精选鹅翅中段，卤香入骨，追剧佐酒首选。', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 326, 'on_sale', NULL, 3, '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_merchant_service` VALUES (4, 2, 'seed_goose_001_dish_4', '卤鹅掌', 28.00, 999, '胶质丰富，Q弹有嚼劲，老少皆宜。', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 715, 'on_sale', NULL, 4, '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_merchant_service` VALUES (5, 2, 'seed_goose_001_dish_5', '卤水拼盘', 88.00, 999, '鹅翅、鹅掌、鹅胗组合装，一次尝遍经典部位。', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 268, 'on_sale', NULL, 5, '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_merchant_service` VALUES (6, 3, 'seed_goose_002_dish_1', '古镇体验半鹅', 58.00, 999, '半只装，适合2-3人分享，附赠卤汁。', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 412, 'on_sale', NULL, 1, '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_merchant_service` VALUES (7, 3, 'seed_goose_002_dish_2', '卤鹅脖', 18.00, 999, '颈段精卤，越啃越香。', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 680, 'on_sale', NULL, 2, '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_merchant_service` VALUES (8, 3, 'seed_goose_002_dish_3', '卤鹅头', 22.00, 999, '鹅头精卤，胶质满满，下酒佳品。', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 295, 'on_sale', NULL, 3, '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_merchant_service` VALUES (9, 4, 'seed_goose_003_dish_1', '非遗整鹅礼盒', 198.00, 999, '整鹅真空包装，伴手礼首选，附非遗证书。', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 186, 'on_sale', NULL, 1, '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_merchant_service` VALUES (10, 4, 'seed_goose_003_dish_2', '卤鹅腿', 45.00, 999, '大腿精段，肉质饱满，单只装。', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 358, 'on_sale', NULL, 2, '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_merchant_service` VALUES (11, 4, 'seed_goose_003_dish_3', '卤鹅胗', 32.00, 999, '爽脆鹅胗，卤香四溢。', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 421, 'on_sale', NULL, 3, '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_merchant_service` VALUES (13, 5, 'MS202609171626097405', '标准大床房', 128.00, 10, '标准大床房，不可加床', '30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{\"inheritStandard\":true,\"area\":\"30\",\"floor\":\"5、6\",\"bedType\":\"大床\",\"bedSize\":\"1.8\",\"extraBedPolicy\":\"不可加床\",\"occupancy\":2,\"bedCount\":1,\"gallery\":[],\"tags\":[],\"facilities\":[]}', 2, 'https://tu.mianyazuju.com/2026/09/17/7d4bb40c-2520-48c2-994a-3bf84e005a55.png', NULL, NULL, 0, 'on_sale', 'hotel', 0, '2026-09-17 16:26:09', '2026-09-17 16:26:09');
INSERT INTO `tb_merchant_service` VALUES (15, 6, 'MS202609211750481892', '探秘南疆11日全景游！坐飞机玩南疆，2999元/人，南疆自然风光+人文一次体验', 2998.00, 0, '喀什古城、博斯腾湖、沙漠公路、巴楚红海胡杨林、盘龙古道、温宿大峡谷、帕米尔高原、喀拉库勒湖、莎车王陵、罗布人村寨沿途风景优美', '3天', '乌鲁木齐市', '喀什古城、博斯腾湖、沙漠公路、巴楚红海胡杨林、盘龙古道、温宿大峡谷、帕米尔高原、喀拉库勒湖、莎车王陵、罗布人村寨', '乌鲁木齐市', 3, '[{\"dayNo\":1,\"dayLabel\":\"第1天\",\"title\":\"各地--乌鲁木齐（飞机/火车）\",\"content\":\"今日体验：逛吃国际大巴扎 红山公园 参观自治区博物馆\\n详细行程：欢迎来到大美新疆！ 免费接机后送您入住酒店休息，然后自由活动，次日出发时间司机和管家会发信息或电话告知，请您注意查收。\\n1、新疆与北京等内地城市有2小时时差，执行北京时间；\\n2、我社出团前会以短信或来电的方式告知您接机方式，请注意保持手机畅通；\\n3、乌鲁木齐距离市区至机场30公里，行车时间在40分钟左右；\\n4、早到攻略：可以自行前往大巴扎，和田街，自治区博物馆（提前预约，周一闭馆）\",\"images\":[\"https://tu.mianyazuju.com/2026/09/21/635177a1-068d-407b-b532-0d5a40afa349.jpg\",\"https://tu.mianyazuju.com/2026/09/21/4e37eb08-11df-4aae-8add-e6d256835e8f.jpg\",\"https://tu.mianyazuju.com/2026/09/21/fae0a81a-eade-4ab3-adbd-4a4cbcd10a45.png\",\"https://tu.mianyazuju.com/2026/09/21/9f0e6e93-b8fb-423e-a004-160e1c5d2ccc.jpg\"]},{\"dayNo\":2,\"dayLabel\":\"第2天\",\"title\":\"乌鲁木齐-乌尉高速(天山胜利隧道)-罗布人村寨-库尔勒（单程约500公里，行车时间约7小时）\",\"content\":\"早餐后，乘车出发沿乌尉高速驶入天山胜利隧道，隧道内通风良好、照明充足，采用 “三洞 + 四竖井” 设计，仅需 20 分钟即可穿越天山，相比传统路线缩短 3.5 小时车程。感受穿越天山的壮阔。随后抵达罗布人村寨，这里是沙漠与绿洲交汇的秘境；\\n傍晚抵达库尔勒，入住酒店，可自由前往孔雀河畔散步，欣赏库尔勒夜景。\",\"images\":[\"https://tu.mianyazuju.com/2026/09/21/f172c1dc-fafb-496b-9802-8c54dadea9ad.jpg\",\"https://tu.mianyazuju.com/2026/09/21/ceddebed-6897-43ae-a0d0-cb4b5cc20bf8.jpg\",\"https://tu.mianyazuju.com/2026/09/21/cb719f9b-a897-4997-969b-176cec082acc.jpg\",\"https://tu.mianyazuju.com/2026/09/21/8b1f763e-c884-4c49-a9dd-d655edce665e.jpg\"]},{\"dayNo\":3,\"dayLabel\":\"第3天\",\"title\":\"库尔勒--温宿大峡谷--阿克苏（单程约595公里，行车时间约8小时）\",\"content\":\"早餐后，乘车前往游览【温宿大峡谷】又叫温宿托尔木大峡谷，温宿大峡谷有中国罕见的远古岩盐地质绝景、中国西部奇特的雅丹地质怪景、中国独有的巨型岩溶蚀地质秘境，堪称新疆“活的地质演变史博物馆”，进入景区，犹如进入了一片精美雄浑的自然画廊。 峡谷中山壁岩层分布清晰，受挤压形成的褶皱，弯曲的线条十分清晰，断裂的岩石夹在山壁岩层中， 在历经亿万年的风雨侵蚀后，形成了绝壁高耸、奇峰兀立、形态各异、嶙峋怪异、色彩浓烈、千姿百 态、五彩纷呈的奇特景观。\",\"images\":[\"https://tu.mianyazuju.com/2026/09/21/5fd5906a-9337-4638-8c62-c2217b096275.jpg\",\"https://tu.mianyazuju.com/2026/09/21/1e1642e0-3d22-4250-b6d8-cbb5e64f74f3.jpg\",\"https://tu.mianyazuju.com/2026/09/21/3f49684a-7a36-481f-bc8a-95060c619b0a.jpg\"]}]', NULL, NULL, NULL, NULL, 'https://tu.mianyazuju.com/2026/09/21/d59104b4-5316-4c53-bc58-22c8b7fa105f.jpg', '[\"https://tu.mianyazuju.com/2026/09/21/408006d0-b718-4a57-9ee3-e520ef6ea3e6.jpg\",\"https://tu.mianyazuju.com/2026/09/21/3335bd40-75d3-4bc5-b38a-16515105a435.jpeg\",\"https://tu.mianyazuju.com/2026/09/21/3895d814-d195-4491-9c00-7627d36761e1.jpg\"]', NULL, 0, 'on_sale', 'route', 0, '2026-09-21 17:50:48', '2026-09-21 17:50:48');
INSERT INTO `tb_merchant_service` VALUES (16, 6, 'MS202609221335032931', '阿勒泰醉秋，【阿勒泰起止】6日纯玩小团，金秋限定，大自然调色盘！我的阿勒泰，三景区连住连玩', 5399.00, 0, '喀纳斯、禾木、白哈巴、阿勒泰喀纳斯、禾木、白哈巴、阿勒泰行程天数6天', '5天', '阿勒泰', '喀纳斯、禾木、白哈巴、阿勒泰', '阿勒泰地区', 5, '[{\"dayNo\":1,\"dayLabel\":\"第1天\",\"title\":\"阿勒泰接机/火车\",\"content\":\"今日主题——准时接机/接站，送到指定酒店\\n推荐活动：逛克兰河，感受北疆初秋\\n温馨提示：\\n新疆与北京等内地城市有2小时时差，执行北京时间；\\n我社出团前会以短信或来电的方式告知您接机方式，请注意保持手机畅通；\\n阿勒泰机场至市区约30公里，行车时间在40分钟左右；\\n秋季阿勒泰早晚温差大，请带好保暖外套。\",\"images\":[\"https://tu.mianyazuju.com/2026/09/22/d5f41314-3d65-45d5-99c0-bc1331ab875e.jpg\",\"https://tu.mianyazuju.com/2026/09/22/c5e92653-6814-428e-8f0a-b9c47ca33db9.jpg\",\"https://tu.mianyazuju.com/2026/09/22/3ea1cba7-6565-41c6-bdf8-65c2f7135583.jpg\",\"https://tu.mianyazuju.com/2026/09/22/60809b17-3df9-48be-931e-d3a142cdfa94.png\"]},{\"dayNo\":2,\"dayLabel\":\"第2天\",\"title\":\"阿勒泰→阿禾公路→禾木（230公里，约5小时）\",\"content\":\"08:00-08:30 酒店早餐\\n09:00-13:00 踏上被誉为“景观大道”的阿禾公路（208公里）。沿途阿勒泰山脉层林尽染，白桦林通体金黄，与墨绿的云杉、湛蓝的天空、皑皑雪山构成震撼的色彩交响乐。\\n13:30-14:30 抵达【禾木村国家5A级景区】，换乘区间车进入。入住特色小木屋。\\n15:00-18:00 漫步禾木村：禾木桥、白桦林、百年老屋。河边的白桦林秋色正浓。\\n18:00-20:00 登上哈登观景台，俯瞰整个村庄。（赠送禾木轻旅拍：含6张精修成片 + 底片全送，不含妆造服饰）\\n20:00后是拍摄璀璨银河和星空的绝佳地点。\",\"images\":[\"https://tu.mianyazuju.com/2026/09/22/8259982b-4089-40f0-a606-972794545b58.jpg\",\"https://tu.mianyazuju.com/2026/09/22/d78c96c6-3ae2-4367-82ee-32404d833f83.jpg\",\"https://tu.mianyazuju.com/2026/09/22/c578d32e-c8d9-483d-9347-bd50b8b08f37.jpg\"]},{\"dayNo\":3,\"dayLabel\":\"第3天\",\"title\":\"禾木→喀纳斯（120公里，约3小时）\",\"content\":\"06:30-08:00 早餐自理，早起（自愿）：登上观景台欣赏禾木晨雾。\\n09:00-09:30 收拾行李，乘坐区间车出村。\\n10:00-11:30 前往贾登峪停车场。\\n11:30-17:00 换乘区间车游览【喀纳斯国家5A级景区】（含门票+一进区间车）：\\n登【观鱼台】（1068级台阶，视体力自选）：站在制高点，将整个喀纳斯湖尽收眼底。湖水如翡翠镶嵌在漫山遍野的金黄、橙红、深绿之中，远眺友谊峰雪顶，探寻“湖怪”传说。\\n漫步三湾：神仙湾、月亮湾、卧龙湾。秋色层次分明，色彩斑斓，随手一拍都是大片。\\n17:30-18:30 夜宿喀纳斯景。\",\"images\":[\"https://tu.mianyazuju.com/2026/09/22/5015a977-605f-4c0d-97c4-2c738cbe88ec.jpg\",\"https://tu.mianyazuju.com/2026/09/22/f854dbeb-e8bf-407f-8d89-a4b72cb259ae.jpg\",\"https://tu.mianyazuju.com/2026/09/22/b85a38a4-66b3-49cc-a2f5-e0da43f49696.jpg\"]},{\"dayNo\":4,\"dayLabel\":\"第4天\",\"title\":\"喀纳斯→铁贾公路→白哈巴（270公里，约4.5小时）\",\"content\":\"08:00-08:30 早餐自理\\n09:00-12:30 吃饱睡足后慢逛喀纳斯，村内及周边遍布金黄色的白桦林，秋季时树叶呈现火红、金黄等色彩，与墨绿的松林形成鲜明对比，构成童话般的油画感。随后乘车前往著名的【神仙湾、卧龙湾、月亮湾】三个打卡点，徒步游三湾。\\n12:30-13:30 午餐自理\\n14:00-17:30 前往【白哈巴】（含门票+区间车）。沿途途经铁贾公路，两侧森林色彩斑斓，如同一幅巨型的秋日画卷。\\n17:30-19:00 白哈巴村庄规模小巧宁静，被五彩斑斓的秋色山林环绕。\\n晚宿：白哈巴景区住宿。\",\"images\":[\"https://tu.mianyazuju.com/2026/09/22/71857cc9-d161-45f4-9833-345097e7aa69.jpg\",\"https://tu.mianyazuju.com/2026/09/22/d217d973-bed9-4959-9251-c6d026c3ee07.jpg\",\"https://tu.mianyazuju.com/2026/09/22/bd15ad73-0750-46a3-a9b5-41ad15b7f9c9.png\"]},{\"dayNo\":5,\"dayLabel\":\"第5天\",\"title\":\"白哈巴→阿勒泰（270公里，约4小时）\",\"content\":\"今日主题——在白桦林间漫步，在边境线上凝望\\n08:30-09:30 睡到自然醒，早餐自理。\\n09:30-12:00 慢逛白哈巴村：村内及周边遍布金黄色的白桦林，秋季时树叶呈现火红、金黄等色彩，与墨绿的松林形成鲜明对比，构成童话般的油画感。中哈边界附近的峡谷两侧植被色彩斑斓，层次分明，是国境线上的巨型自然画卷。\\n12:00-13:00 午餐自理\\n13:00-17:00 沿G219国道返回阿勒泰。沿途欣赏边境公路的壮阔秋色。\\n17:30 抵达阿勒泰，入住携程4钻酒店。晚餐可自由探索阿勒泰市区美食。\",\"images\":[\"https://tu.mianyazuju.com/2026/09/22/1b925106-34c6-4728-a94e-1f15c12378e2.jpg\",\"https://tu.mianyazuju.com/2026/09/22/8dac58bb-a98b-438a-a11f-d31b61aec50c.jpg\",\"https://tu.mianyazuju.com/2026/09/22/9f75fa7f-906e-47a1-b03c-e86005272bec.jpg\",\"https://tu.mianyazuju.com/2026/09/22/c91669ad-a476-47e5-8659-ab7d46f25faf.png\"]}]', NULL, NULL, NULL, NULL, 'https://tu.mianyazuju.com/2026/09/23/7d39fb59-3db2-456d-9175-2f4bc40e13a4.jpg', '[\"https://tu.mianyazuju.com/2026/09/23/1d9de0a1-61e3-422e-af79-9d9b77592e73.jpg\",\"https://tu.mianyazuju.com/2026/09/23/5b312731-e8e3-4bc8-bc3d-17090dbd4411.jpg\",\"https://tu.mianyazuju.com/2026/09/23/c4f881ae-a59f-4bc3-8d6c-15941c485764.jpg\",\"https://tu.mianyazuju.com/2026/09/23/a7475ef6-77ba-43c8-aad7-a8bbb20182ef.png\"]', NULL, 0, 'on_sale', 'route', 0, '2026-09-22 13:35:03', '2026-09-22 13:35:03');
INSERT INTO `tb_merchant_service` VALUES (17, 7, 'MS202609221359545133', '鸡汤丸子', 28.00, 0, '鸡汤丸子是一道家常暖胃菜。鲜嫩的猪肉丸子滑入清亮的鸡汤中，文火慢炖，汤色渐浓，香气四溢。丸子吸饱了汤汁，入口弹嫩，汤头鲜美醇厚。撒上葱花与香菜，一碗下肚，暖意从胃里蔓延开来，是冬日里最贴心的慰藉。', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'https://tu.mianyazuju.com/2026/09/22/a75b553d-9dce-44d1-a620-1aac72e98f69.png', '[\"https://tu.mianyazuju.com/2026/09/22/a10e918b-0ba8-452f-9565-dff3aa81ca46.png\",\"https://tu.mianyazuju.com/2026/09/22/dc13b5f6-a334-449a-96c1-196d1d193352.png\",\"https://tu.mianyazuju.com/2026/09/22/fd697250-9e0e-4072-b8be-4f632395329a.webp\"]', NULL, 128, 'on_sale', 'activity', 0, '2026-09-22 13:59:55', '2026-09-22 13:59:54');
INSERT INTO `tb_merchant_service` VALUES (19, 8, 'MS202609231927213810', '西安毛绒玩具文创', 15.90, 90, '好物简介西安毛绒玩具文创多种品类西安毛绒玩具文创多种品类西安毛绒玩具文创多种品类西安毛绒玩具文创多种品类西安毛绒玩具文创多种品类', NULL, NULL, NULL, NULL, NULL, NULL, '{\"mode\":\"multi\",\"skus\":[{\"name\":\"大雁塔毛绒文创\",\"price\":15.9,\"stock\":44},{\"name\":\"秦岭大熊猫文创\",\"price\":25.9,\"stock\":46}]}', '西安毛绒玩具文创多种品类西安毛绒玩具文创多种品类西安毛绒玩具文创多种品类西安毛绒玩具文创多种品类西安毛绒玩具文创多种品类', NULL, NULL, 'https://tu.mianyazuju.com/2026/09/23/57b83633-34e9-476c-bde9-c0e0e1b5d249.png', '[\"https://tu.mianyazuju.com/2026/09/23/e542f1b5-111d-4eda-b4ef-4aaaa3c2c8dd.png\",\"https://tu.mianyazuju.com/2026/09/23/9eddf09e-4e0d-4abe-8ecb-0fd0c154c9c6.png\",\"https://tu.mianyazuju.com/2026/09/23/1f51748d-90e5-4443-8cd0-1631a300ee15.webp\"]', '[\"https://tu.mianyazuju.com/2026/09/23/bbc9b883-03de-40d8-a569-dcfba5d05fff.png\"]', 0, 'on_sale', 'heritage', 0, '2026-09-23 19:27:21', '2026-09-24 16:22:33');

-- ----------------------------
-- Table structure for tb_merchant_store_decor
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_store_decor`;
CREATE TABLE `tb_merchant_store_decor`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id` bigint(0) NOT NULL COMMENT '商户ID',
  `decor_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '装修内容JSON',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_merchant_store_decor_merchant_id`(`merchant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户门店装修' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_merchant_store_decor
-- ----------------------------
INSERT INTO `tb_merchant_store_decor` VALUES (1, 2, '{\"bannerList\":[],\"badgeText\":\"非遗美食\",\"tags\":[\"百年传承\",\"地道口味\",\"可堂食\"],\"rating\":\"4.9\",\"monthlySales\":1286,\"intro\":\"老字号·荣昌卤鹅旗舰店始创于1986年，传承三代卤制技艺，选用本地散养白鹅，以二十余味天然香料慢火卤制六小时。\",\"businessHours\":[{\"id\":1,\"label\":\"周一至周五\",\"time\":\"08:30 - 20:30\"},{\"id\":2,\"label\":\"周六至周日\",\"time\":\"08:00 - 21:00\"}]}', '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_merchant_store_decor` VALUES (2, 3, '{\"bannerList\":[],\"badgeText\":\"非遗美食\",\"tags\":[\"非遗技艺\",\"古镇特色\",\"可外带\"],\"rating\":\"4.8\",\"monthlySales\":956,\"intro\":\"荣昌卤鹅·万灵古镇体验店位于万灵古镇核心街区，由市级非遗传承人亲自主理。\",\"businessHours\":[{\"id\":1,\"label\":\"周一至周日\",\"time\":\"09:00 - 20:00\"}]}', '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_merchant_store_decor` VALUES (3, 4, '{\"bannerList\":[],\"badgeText\":\"非遗美食\",\"tags\":[\"万灵古镇\",\"非遗技艺\",\"文化体验\"],\"rating\":\"4.7\",\"monthlySales\":743,\"intro\":\"吴记非遗卤鹅馆位于万灵古镇核心街区，融合卤鹅制作展示与品鉴体验。\",\"businessHours\":[{\"id\":1,\"label\":\"周一至周五\",\"time\":\"09:30 - 18:30\"},{\"id\":2,\"label\":\"周六至周日\",\"time\":\"09:00 - 19:00\"}]}', '2026-09-10 09:44:08', '2026-09-10 09:44:08');
INSERT INTO `tb_merchant_store_decor` VALUES (4, 5, '{\"cover\":\"https://tu.mianyazuju.com/2026/09/17/fa8eb20e-0e14-4847-a490-18c94a8fa8fd.jpg\",\"checkOutTime\":\"12:00 前退房\",\"starLabel\":\"四星级\",\"phone\":\"18892020826\",\"checkInTime\":\"14:00 后入住\",\"intro\":\"酒店位于环境怡人、风景秀丽、旅游资源丰富、历史文化浓郁的的新县大别山腹地，是一座集度假、会议、住宿、餐饮、休闲娱乐多功能为一体的高端度假酒店。作为接待中央及省、市领导的政商名流云集之地，是您迎春踏青、消夏避暑、金秋赏月、冬觅清幽的上佳之选。酒店总建筑面积达 112000 m2、拥有豪华客房多间、属信阳地区设施最完备豪华酒店度假群，是各类会议、商旅团队及度假游客的优选之地。\",\"starLevel\":4,\"hotelPolicy\":{\"bookingNotice\":\"订单需等酒店或供应商确认后生效，确认结果以短信、邮件或 App 通知为准。\",\"checkInNotice\":\"前台 24 小时服务，请于入住时在柜台办理登记。\",\"depositPolicy\":\"不收取押金\",\"breakfastInfo\":\"类型：自助餐 菜品：中式 营业时间：周一至周日07:00-09:00开放\"},\"facilities\":[{\"id\":1,\"icon\":\"wifi\",\"label\":\"免费WiFi\"},{\"id\":2,\"icon\":\"car-fill\",\"label\":\"免费停车\"},{\"id\":3,\"icon\":\"grid-fill\",\"label\":\"健身房\"},{\"id\":4,\"icon\":\"gift-fill\",\"label\":\"含早餐\"}],\"bannerImages\":[\"https://tu.mianyazuju.com/2026/09/17/e72b3857-e276-4ad1-94f9-ad6a8b467d47.jpg\",\"https://tu.mianyazuju.com/2026/09/17/c7c42056-8e75-4a23-9b97-534f14077585.jpeg\",\"https://tu.mianyazuju.com/2026/09/17/090cd7e9-0f32-448f-82b4-db9e1bd36c09.jpg\"],\"updatedAt\":1789635282558}', '2026-09-17 10:42:22', '2026-09-17 10:42:21');
INSERT INTO `tb_merchant_store_decor` VALUES (5, 6, '{\"cover\":\"https://tu.mianyazuju.com/2026/09/21/7fc1aae1-1137-4232-842a-03fd96377947.jpg\",\"duration\":\"1天\",\"guides\":[{\"name\":\"导游小明\",\"avatar\":\"https://tu.mianyazuju.com/2026/09/21/b79f8bcd-2647-4f2c-bd07-e3e85ca7136b.jpg\"},{\"name\":\"导游小李\",\"avatar\":\"https://tu.mianyazuju.com/2026/09/21/4a0e3b24-7a10-481a-a26d-07b845435b0f.jpg\"}],\"meetPoint\":\"陕西省西安市新城区关中巷十七旅行社\",\"intro\":\"十七旅行社精选旅游线路，专业导游带队，安心出行。\",\"spots\":[\"核心景点\",\"特色体验\"],\"itinerary\":[{\"name\":\"集合出发\",\"location\":\"门店/集合点\",\"time\":\"09:00\",\"desc\":\"请准时到达集合地点\"},{\"name\":\"核心体验\",\"location\":\"行程中途\",\"time\":\"12:00\",\"desc\":\"体验当地特色项目\"}],\"tags\":[\"精品小团\",\"含讲解\",\"多回头客\"],\"updatedAt\":1789987163575,\"bannerImages\":[\"https://tu.mianyazuju.com/2026/09/21/9b9c963e-a75e-47db-acb7-e5b9f9fb1136.png\",\"https://tu.mianyazuju.com/2026/09/21/584ed607-a789-4ff4-939a-4c0cf89e65c6.jpg\",\"https://tu.mianyazuju.com/2026/09/21/e94b9f0d-0a68-4176-a59c-1be4a2dff8ff.jpg\"],\"wechatQrCode\":\"https://tu.mianyazuju.com/2026/09/21/a8b5161f-5a52-4dbf-8a29-b72d05fa4e58.jpg\"}', '2026-09-21 15:00:17', '2026-09-21 15:00:17');
INSERT INTO `tb_merchant_store_decor` VALUES (6, 7, '{\"cover\":\"https://tu.mianyazuju.com/2026/09/22/173ed6e7-8388-4323-aa67-0d65683c7e7e.webp\",\"bannerList\":[\"https://tu.mianyazuju.com/2026/09/22/3f1d2044-94fa-42c0-aad5-8bbe8ed46558.png\",\"https://tu.mianyazuju.com/2026/09/22/975cc9d7-efcb-4f25-a123-0bab154b1f6f.jpg\",\"https://tu.mianyazuju.com/2026/09/22/4d5983f7-12dc-4052-9a53-4048e33c2051.png\",\"https://tu.mianyazuju.com/2026/09/22/1ee8ec22-c415-47d3-a774-ca2ee1b4db32.png\"],\"intro\":\"十八陕菜馆欢迎您的光临，传承地道风味，支持在线预约到店用餐。\",\"rating\":\"4.8\",\"businessHours\":[{\"id\":1,\"label\":\"周一至周五\",\"time\":\"09:00 - 21:00\"},{\"id\":2,\"label\":\"周六至周日\",\"time\":\"09:00 - 22:00\"}],\"badgeText\":\"非遗美食\",\"monthlySales\":150,\"tags\":[\"可堂食\",\"支持预约\",\"可预约\"],\"updatedAt\":1790056702873}', '2026-09-22 13:16:13', '2026-09-22 13:16:13');
INSERT INTO `tb_merchant_store_decor` VALUES (7, 8, '{\"cover\":\"https://tu.mianyazuju.com/2026/09/23/f1ccf878-bb3f-44d2-a0c1-47f7f439c794.webp\",\"reviewCount\":0,\"intro\":\"叁拾文创专卖店专注荣昌非遗文创与手作体验。\",\"rating\":\"4.8\",\"introDetail\":\"叁拾文创专卖店专注荣昌非遗文创与手作体验。\",\"services\":[\"文创销售\",\"手作体验\"],\"tags\":[\"非遗文创\",\"可体验\"],\"updatedAt\":1790226149759,\"bannerImages\":[\"https://tu.mianyazuju.com/2026/09/24/0f4e4aef-4978-45e3-8aa7-fd24cb52430b.png\",\"https://tu.mianyazuju.com/2026/09/24/e1710c33-108e-4be9-8ce8-b609a92a3181.jpeg\",\"https://tu.mianyazuju.com/2026/09/24/c4fa75ba-a94e-4362-9659-475ade3a55bb.webp\",\"https://tu.mianyazuju.com/2026/09/24/f5c4ad9d-0310-4b21-8412-d85f1c2d1e88.jpg\"],\"shipping\":{\"freeShipping\":false,\"fixedFee\":8,\"freeAmountThreshold\":0,\"shippingNote\":\"新疆西藏等偏远地区请联系客服\"}}', '2026-09-23 17:42:14', '2026-09-23 17:42:13');

-- ----------------------------
-- Table structure for tb_merchant_wallet
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_wallet`;
CREATE TABLE `tb_merchant_wallet`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id` bigint(0) NOT NULL COMMENT '商户ID',
  `balance` decimal(12, 2) NOT NULL COMMENT '可提现余额',
  `pending_settle` decimal(12, 2) NOT NULL COMMENT '待结算金额',
  `total_income` decimal(12, 2) NOT NULL COMMENT '累计收入',
  `deposit_required` decimal(12, 2) NOT NULL DEFAULT 5000.00 COMMENT '应缴保证金',
  `deposit_paid` decimal(12, 2) NOT NULL COMMENT '已缴保证金',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_merchant_wallet_merchant_id`(`merchant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户钱包' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_merchant_wallet
-- ----------------------------
INSERT INTO `tb_merchant_wallet` VALUES (1, 4, 0.00, 0.00, 0.00, 5000.00, 0.00, '2026-09-11 10:45:44', '2026-09-11 10:45:44');
INSERT INTO `tb_merchant_wallet` VALUES (2, 1, 0.00, 0.00, 0.00, 5000.00, 0.00, '2026-09-14 13:53:04', '2026-09-14 13:53:03');
INSERT INTO `tb_merchant_wallet` VALUES (3, 5, 4350.00, 0.00, 0.00, 1.00, 1.00, '2026-09-16 10:17:22', '2026-09-21 11:38:37');
INSERT INTO `tb_merchant_wallet` VALUES (4, 6, 0.00, 0.00, 0.00, 5.00, 5.00, '2026-09-21 14:27:21', '2026-09-21 14:30:52');
INSERT INTO `tb_merchant_wallet` VALUES (5, 7, 0.00, 0.00, 0.00, 3000.00, 3000.00, '2026-09-22 11:40:51', '2026-09-22 13:10:04');
INSERT INTO `tb_merchant_wallet` VALUES (6, 8, 23.90, 0.00, 0.00, 2000.00, 2000.00, '2026-09-23 15:42:59', '2026-09-24 16:11:33');

-- ----------------------------
-- Table structure for tb_merchant_wallet_transaction
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_wallet_transaction`;
CREATE TABLE `tb_merchant_wallet_transaction`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id` bigint(0) NOT NULL COMMENT '商户ID',
  `tx_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流水号',
  `tx_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型：deposit/withdraw/income/settle',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流水标题',
  `amount` decimal(12, 2) NOT NULL COMMENT '变动金额（正收入负支出）',
  `balance_after` decimal(12, 2) NOT NULL COMMENT '变动后可提现余额',
  `ref_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联业务单号（订单号/提现单号）',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1成功 0处理中 2失败',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_merchant_wallet_tx_no`(`tx_no`) USING BTREE,
  INDEX `idx_merchant_wallet_tx_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_merchant_wallet_tx_type`(`tx_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户钱包流水' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_merchant_wallet_transaction
-- ----------------------------
INSERT INTO `tb_merchant_wallet_transaction` VALUES (1, 5, '202609201034443515', 'deposit', '保证金缴纳', 5000.00, 0.00, 'MD202609201033395010', 1, '2026-09-20 10:34:45');
INSERT INTO `tb_merchant_wallet_transaction` VALUES (2, 5, '202609201353591869', 'income', '保证金退还', 5000.00, 5000.00, 'DR202609201353469086', 1, '2026-09-20 13:53:59');
INSERT INTO `tb_merchant_wallet_transaction` VALUES (3, 5, '202609201353596136', 'deposit_refund', '保证金退还', -5000.00, 5000.00, 'DR202609201353469086', 1, '2026-09-20 13:53:59');
INSERT INTO `tb_merchant_wallet_transaction` VALUES (4, 5, '202609211127292233', 'deposit', '保证金缴纳', 1.00, 5000.00, 'MD202609211127282454', 1, '2026-09-21 11:27:30');
INSERT INTO `tb_merchant_wallet_transaction` VALUES (5, 5, '202609211131414508', 'withdraw', '提现至支付宝', -500.00, 4500.00, 'WD202609211131419171', 0, '2026-09-21 11:31:42');
INSERT INTO `tb_merchant_wallet_transaction` VALUES (6, 5, '202609211138371814', 'withdraw', '提现至微信', -150.00, 4350.00, 'WD202609211138379161', 0, '2026-09-21 11:38:37');
INSERT INTO `tb_merchant_wallet_transaction` VALUES (7, 6, '202609211430524573', 'deposit', '保证金缴纳', 5.00, 0.00, 'MD202609211430512064', 1, '2026-09-21 14:30:52');
INSERT INTO `tb_merchant_wallet_transaction` VALUES (8, 7, '202609221310043455', 'deposit', '保证金缴纳', 3000.00, 0.00, 'MD202609221310017630', 1, '2026-09-22 13:10:04');
INSERT INTO `tb_merchant_wallet_transaction` VALUES (9, 8, '202609231543445588', 'deposit', '保证金缴纳', 2000.00, 0.00, 'MD202609231543439433', 1, '2026-09-23 15:43:44');
INSERT INTO `tb_merchant_wallet_transaction` VALUES (10, 8, 'GSB2026092416113300000008', 'settle', '好物订单结算补录(1笔)', 23.90, 23.90, NULL, 1, '2026-09-24 16:11:33');

-- ----------------------------
-- Table structure for tb_notice
-- ----------------------------
DROP TABLE IF EXISTS `tb_notice`;
CREATE TABLE `tb_notice`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `summary` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '摘要（列表/跑马灯展示）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告正文',
  `category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'policy' COMMENT '分类：activity/scenic/traffic/policy',
  `source` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '来源机构',
  `destination_id` bigint(0) NULL DEFAULT NULL COMMENT '关联目的地ID，NULL表示全平台',
  `pinned` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否置顶：1是 0否',
  `publish_time` datetime(0) NOT NULL COMMENT '发布时间',
  `expire_time` datetime(0) NULL DEFAULT NULL COMMENT '过期时间，NULL表示长期有效',
  `state` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态：1已发布 0草稿',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序，升序',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notice_destination_id`(`destination_id`) USING BTREE,
  INDEX `idx_notice_state`(`state`) USING BTREE,
  INDEX `idx_notice_publish_time`(`publish_time`) USING BTREE,
  INDEX `idx_notice_category`(`category`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '平台公告' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_notice
-- ----------------------------
INSERT INTO `tb_notice` VALUES (2, '景区预约入园须知', '热门景区建议提前预约，入园请携带有效身份证件。', '为保障游览体验，万灵古镇、安陶小镇等热门景区在节假日实行预约入园。\n\n预约方式：荣e游小程序 → 景区旅游 → 选择景区 → 立即预约。\n\n入园时请出示预约凭证及有效证件，配合工作人员指引。', 'scenic', '景区管理中心', 1, 0, '2026-07-28 10:00:00', NULL, 1, 2, '2026-09-10 09:43:32', '2026-09-16 10:00:43');
INSERT INTO `tb_notice` VALUES (4, '西安热门景区实名预约提示', '兵马俑、陕历博、城墙等须提前线上预约，现场不售票或限流。', '根据各景区最新公告：\n\n1. 秦始皇帝陵博物院实行实名线上预约，旺季建议提前7-10天通过「秦始皇帝陵博物院」官方公众号购票；\n2. 陕西历史博物馆基本陈列免费，须关注「陕西历史博物馆」微信公众号，提前5天17:00抢票；\n3. 西安城墙景区须通过「西安城墙景区」小程序预约，不设线下售票窗口；\n4. 华清宫可通过「华清宫景区」官方渠道预约。\n\n请携带本人身份证，按预约时段入园。', 'scenic', '西安市文化和旅游局', 4, 1, '2026-06-01 09:00:00', NULL, 1, 1, '2026-09-10 09:44:16', '2026-09-16 14:49:42');

-- ----------------------------
-- Table structure for tb_order
-- ----------------------------
DROP TABLE IF EXISTS `tb_order`;
CREATE TABLE `tb_order`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号（业务键）',
  `user_id` bigint(0) NOT NULL COMMENT '下单用户ID',
  `merchant_id` bigint(0) NOT NULL COMMENT '商户ID',
  `destination_id` bigint(0) NOT NULL COMMENT '目的地ID',
  `order_category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单分类：hotel/route/goods',
  `order_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订单类型：hotel/route/goods/culture/service/restaurant',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '订单状态：0待支付 10待确认 20待使用 30待收货 40已完成 50已取消 60退款中',
  `status_text` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态展示文案',
  `total_amount` decimal(12, 2) NOT NULL COMMENT '订单总额',
  `pay_amount` decimal(12, 2) NOT NULL COMMENT '实付金额',
  `refund_amount` decimal(12, 2) NULL DEFAULT NULL COMMENT '用户退款金额',
  `merchant_settle_amount` decimal(12, 2) NULL DEFAULT NULL COMMENT '商户结算入账金额',
  `platform_fee_amount` decimal(12, 2) NULL DEFAULT NULL COMMENT '平台抽成金额',
  `settle_time` datetime(0) NULL DEFAULT NULL COMMENT '商户结算完成时间',
  `discount_amount` decimal(12, 2) NOT NULL COMMENT '优惠抵扣金额',
  `coupon_id` bigint(0) NULL DEFAULT NULL COMMENT '使用的用户优惠券ID',
  `pay_method` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付方式',
  `pay_time` datetime(0) NULL DEFAULT NULL COMMENT '支付时间',
  `pay_expire_time` datetime(0) NULL DEFAULT NULL COMMENT '支付截止时间',
  `verify_time` datetime(0) NULL DEFAULT NULL COMMENT '核销/入住确认时间',
  `verify_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '核销码',
  `contact_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人手机号',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `price_snapshot_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '下单价格快照JSON',
  `cancel_policy_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '退改规则快照JSON',
  `check_in_date` date NULL DEFAULT NULL COMMENT '入住日期（酒店）',
  `check_out_date` date NULL DEFAULT NULL COMMENT '离店日期（酒店）',
  `nights` int(0) NULL DEFAULT NULL COMMENT '入住晚数（酒店）',
  `room_count` int(0) NOT NULL DEFAULT 1 COMMENT '预订间数',
  `guest_names` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '入住人/出行人姓名',
  `guest_info_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'guest info json',
  `travel_date` date NULL DEFAULT NULL COMMENT '出行日期（线路）',
  `guest_count` int(0) NULL DEFAULT NULL COMMENT '出行人数（线路/活动）',
  `expected_arrival_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '预计到店时间',
  `meet_point` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '集合地点（线路）',
  `receiver_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货人（商品）',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货电话（商品）',
  `receiver_address` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货地址（商品）',
  `merchant_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户名称（冗余展示）',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no`) USING BTREE,
  INDEX `idx_order_user_id`(`user_id`) USING BTREE,
  INDEX `idx_order_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_order_destination_id`(`destination_id`) USING BTREE,
  INDEX `idx_order_status`(`status`) USING BTREE,
  INDEX `idx_order_category`(`order_category`) USING BTREE,
  INDEX `idx_tb_order_verify_code`(`verify_code`) USING BTREE,
  INDEX `idx_tb_order_order_type`(`order_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_order
-- ----------------------------
INSERT INTO `tb_order` VALUES (1, 'RT20260908001', 1, 1, 1, 'route', 'route', 40, '已完成', 198.00, 198.00, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, '2026-09-10 09:43:32', NULL, NULL, NULL, '王先生', '13800002001', '靠窗优先', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, '2026-09-15', 2, NULL, NULL, NULL, NULL, NULL, '荣昌文旅种子商户', '2026-09-10 09:43:32', '2026-09-18 10:33:38');
INSERT INTO `tb_order` VALUES (2, 'RT20260907002', 1, 1, 1, 'route', 'route', 40, '已完成', 396.00, 396.00, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, '2026-09-09 09:43:33', NULL, NULL, NULL, '李女士', '13900001234', '', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, '2026-09-12', 4, NULL, NULL, NULL, NULL, NULL, '荣昌文旅种子商户', '2026-09-10 09:43:33', '2026-09-18 10:33:38');
INSERT INTO `tb_order` VALUES (3, 'HT1789638031264709', 2, 5, 1, 'hotel', 'hotel', 0, '待支付', 128.00, 123.00, NULL, NULL, NULL, NULL, 5.00, 1, NULL, NULL, NULL, NULL, NULL, '十六', '18892020826', '备注备注阿三大苏打实打实的', NULL, NULL, '2026-09-17', '2026-09-18', 1, 1, '十六', NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, '石榴大酒店', '2026-09-17 17:40:31', '2026-09-18 10:33:38');
INSERT INTO `tb_order` VALUES (4, 'HT1789639253362815', 2, 5, 1, 'hotel', 'hotel', 0, '待支付', 128.00, 123.00, NULL, NULL, NULL, NULL, 5.00, 1, NULL, NULL, NULL, NULL, NULL, '十六', '18892020826', '十六的备注', NULL, NULL, '2026-09-17', '2026-09-18', 1, 1, '十六', NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, '石榴大酒店', '2026-09-17 18:00:53', '2026-09-18 10:33:38');
INSERT INTO `tb_order` VALUES (5, 'HT1789639481728115', 2, 5, 1, 'hotel', 'hotel', 50, '已取消', 128.00, 123.00, NULL, NULL, NULL, NULL, 5.00, 1, 'wallet', '2026-09-17 18:04:43', NULL, NULL, NULL, '十六', '18892020826', '十六的备注', NULL, NULL, '2026-09-17', '2026-09-18', 1, 1, '十六', NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, '石榴大酒店', '2026-09-17 18:04:42', '2026-09-18 10:33:38');
INSERT INTO `tb_order` VALUES (6, 'HT1789701510116387', 2, 5, 1, 'hotel', 'hotel', 0, '待支付', 128.00, 123.00, NULL, NULL, NULL, NULL, 5.00, 1, NULL, NULL, NULL, NULL, NULL, '十六', '18892020826', '撒大苏打撒旦', NULL, NULL, '2026-09-18', '2026-09-19', 1, 1, '十六', NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, '石榴大酒店', '2026-09-18 11:18:30', '2026-09-18 11:18:30');
INSERT INTO `tb_order` VALUES (7, 'HT1789704334238915', 2, 5, 1, 'hotel', 'hotel', 50, '已取消', 128.00, 128.00, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, '2026-09-18 12:35:34', NULL, NULL, 'QA测试', '18892020826', NULL, '{\"hotelId\":5,\"hotelName\":\"石榴大酒店\",\"hotelAddress\":\"奥尊游泳朗陈20楼\",\"hotelPhone\":\"18892020826\",\"roomId\":10,\"roomName\":\"标准大床房\",\"roomSpec\":\"30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物\",\"basePrice\":128.00,\"checkInDate\":\"2026-09-25\",\"checkOutDate\":\"2026-09-26\",\"nights\":1,\"roomCount\":1,\"dailyPrices\":[{\"date\":\"2026-09-25\",\"unitPrice\":128.00}],\"totalAmount\":128.00,\"payAmount\":128.00}', '{\"policyCode\":\"free_before_checkin_18\",\"freeCancelHour\":18,\"summary\":\"入住日18:00前可免费取消\",\"holdUntilHour\":18}', '2026-09-25', '2026-09-26', 1, 1, 'QA测试', NULL, NULL, 1, '18:00 前', NULL, NULL, NULL, NULL, '石榴大酒店', '2026-09-18 12:05:34', '2026-09-18 12:05:34');
INSERT INTO `tb_order` VALUES (8, 'HT1789704334409472', 2, 5, 1, 'hotel', 'hotel', 50, '已取消', 128.00, 128.00, NULL, NULL, NULL, NULL, 0.00, NULL, 'wallet', '2026-09-18 12:05:34', '2026-09-18 12:35:34', NULL, '067528', 'QA测试', '18892020826', '用户申请退款：QA测试退款', '{\"hotelId\":5,\"hotelName\":\"石榴大酒店\",\"hotelAddress\":\"奥尊游泳朗陈20楼\",\"hotelPhone\":\"18892020826\",\"roomId\":10,\"roomName\":\"标准大床房\",\"roomSpec\":\"30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物\",\"basePrice\":128.00,\"checkInDate\":\"2026-09-25\",\"checkOutDate\":\"2026-09-26\",\"nights\":1,\"roomCount\":1,\"dailyPrices\":[{\"date\":\"2026-09-25\",\"unitPrice\":128.00}],\"totalAmount\":128.00,\"payAmount\":128.00}', '{\"policyCode\":\"free_before_checkin_18\",\"freeCancelHour\":18,\"summary\":\"入住日18:00前可免费取消\",\"holdUntilHour\":18}', '2026-09-25', '2026-09-26', 1, 1, 'QA测试', NULL, NULL, 1, '18:00 前', NULL, NULL, NULL, NULL, '石榴大酒店', '2026-09-18 12:05:34', '2026-09-18 12:05:34');
INSERT INTO `tb_order` VALUES (9, 'HT1789704334658237', 2, 5, 1, 'hotel', 'hotel', 40, '已完成', 256.00, 256.00, NULL, NULL, NULL, NULL, 0.00, NULL, 'wallet', '2026-09-18 12:05:35', '2026-09-18 12:35:35', '2026-09-18 12:05:35', '641196', 'QA测试', '18892020826', NULL, '{\"hotelId\":5,\"hotelName\":\"石榴大酒店\",\"hotelAddress\":\"奥尊游泳朗陈20楼\",\"hotelPhone\":\"18892020826\",\"roomId\":10,\"roomName\":\"标准大床房\",\"roomSpec\":\"30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物\",\"basePrice\":128.00,\"checkInDate\":\"2026-09-25\",\"checkOutDate\":\"2026-09-26\",\"nights\":1,\"roomCount\":2,\"dailyPrices\":[{\"date\":\"2026-09-25\",\"unitPrice\":128.00}],\"totalAmount\":256.00,\"payAmount\":256.00}', '{\"policyCode\":\"free_before_checkin_18\",\"freeCancelHour\":18,\"summary\":\"入住日18:00前可免费取消\",\"holdUntilHour\":18}', '2026-09-25', '2026-09-26', 1, 2, 'QA测试', NULL, NULL, 1, '18:00 前', NULL, NULL, NULL, NULL, '石榴大酒店', '2026-09-18 12:05:35', '2026-09-18 12:05:34');
INSERT INTO `tb_order` VALUES (10, 'HT1789704334832832', 2, 5, 1, 'hotel', 'hotel', 50, '已取消', 128.00, 128.00, NULL, NULL, NULL, NULL, 0.00, NULL, 'wallet', '2026-09-18 12:05:35', '2026-09-18 12:35:35', NULL, '804278', 'QA测试', '18892020826', '用户免费取消', '{\"hotelId\":5,\"hotelName\":\"石榴大酒店\",\"hotelAddress\":\"奥尊游泳朗陈20楼\",\"hotelPhone\":\"18892020826\",\"roomId\":10,\"roomName\":\"标准大床房\",\"roomSpec\":\"30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物\",\"basePrice\":128.00,\"checkInDate\":\"2026-09-28\",\"checkOutDate\":\"2026-09-29\",\"nights\":1,\"roomCount\":1,\"dailyPrices\":[{\"date\":\"2026-09-28\",\"unitPrice\":128.00}],\"totalAmount\":128.00,\"payAmount\":128.00}', '{\"policyCode\":\"free_before_checkin_18\",\"freeCancelHour\":18,\"summary\":\"入住日18:00前可免费取消\",\"holdUntilHour\":18}', '2026-09-28', '2026-09-29', 1, 1, 'QA测试', NULL, NULL, 1, '18:00 前', NULL, NULL, NULL, NULL, '石榴大酒店', '2026-09-18 12:05:35', '2026-09-18 12:05:34');
INSERT INTO `tb_order` VALUES (11, 'HT1789712348595767', 2, 5, 1, 'hotel', 'hotel', 50, '已取消', 128.00, 123.00, NULL, NULL, NULL, NULL, 5.00, 1, NULL, NULL, '2026-09-18 14:49:09', NULL, NULL, '十六-订单取消', '18892020826', '十六测试订单取消', '{\"hotelId\":5,\"hotelName\":\"石榴大酒店\",\"hotelAddress\":\"奥尊游泳朗陈20楼\",\"hotelPhone\":\"18892020826\",\"roomId\":10,\"roomName\":\"标准大床房\",\"roomSpec\":\"30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物\",\"basePrice\":128.00,\"checkInDate\":\"2026-09-18\",\"checkOutDate\":\"2026-09-19\",\"nights\":1,\"roomCount\":1,\"dailyPrices\":[{\"date\":\"2026-09-18\",\"unitPrice\":128.00}],\"totalAmount\":128.00,\"payAmount\":128.00}', '{\"policyCode\":\"free_before_checkin_18\",\"freeCancelHour\":18,\"summary\":\"入住日18:00前可免费取消\",\"holdUntilHour\":18}', '2026-09-18', '2026-09-19', 1, 1, '十六-订单取消', NULL, NULL, 1, '18:00 前', NULL, NULL, NULL, NULL, '石榴大酒店', '2026-09-18 14:19:09', '2026-09-18 14:19:08');
INSERT INTO `tb_order` VALUES (12, 'HT1789714105261153', 2, 5, 1, 'hotel', 'hotel', 50, '已取消', 128.00, 123.00, NULL, NULL, NULL, NULL, 5.00, 1, 'wallet', '2026-09-18 14:48:27', '2026-09-18 15:18:25', NULL, '027315', '十六', '18892020826', '十六测试支付成功取消；用户免费取消', '{\"hotelId\":5,\"hotelName\":\"石榴大酒店\",\"hotelAddress\":\"奥尊游泳朗陈20楼\",\"hotelPhone\":\"18892020826\",\"roomId\":10,\"roomName\":\"标准大床房\",\"roomSpec\":\"30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物\",\"basePrice\":128.00,\"checkInDate\":\"2026-09-18\",\"checkOutDate\":\"2026-09-19\",\"nights\":1,\"roomCount\":1,\"dailyPrices\":[{\"date\":\"2026-09-18\",\"unitPrice\":128.00}],\"totalAmount\":128.00,\"payAmount\":128.00}', '{\"policyCode\":\"free_before_checkin_18\",\"freeCancelHour\":18,\"summary\":\"入住日18:00前可免费取消\",\"holdUntilHour\":18}', '2026-09-18', '2026-09-19', 1, 1, '十六', NULL, NULL, 1, '18:00 前', NULL, NULL, NULL, NULL, '石榴大酒店', '2026-09-18 14:48:25', '2026-09-18 14:48:26');
INSERT INTO `tb_order` VALUES (13, 'HT1789717461338462', 2, 5, 1, 'hotel', 'hotel', 50, '已取消', 128.00, 128.00, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, '2026-09-18 16:14:21', NULL, NULL, '十六', '18892020826', '十六测试核销到评价', '{\"hotelId\":5,\"hotelName\":\"石榴大酒店\",\"hotelAddress\":\"奥尊游泳朗陈20楼\",\"hotelPhone\":\"18892020826\",\"roomId\":10,\"roomName\":\"标准大床房\",\"roomSpec\":\"30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物\",\"basePrice\":128.00,\"checkInDate\":\"2026-09-18\",\"checkOutDate\":\"2026-09-19\",\"nights\":1,\"roomCount\":1,\"dailyPrices\":[{\"date\":\"2026-09-18\",\"unitPrice\":128.00}],\"totalAmount\":128.00,\"payAmount\":128.00}', '{\"policyCode\":\"free_before_checkin_18\",\"freeCancelHour\":18,\"summary\":\"入住日18:00前可免费取消\",\"holdUntilHour\":18}', '2026-09-18', '2026-09-19', 1, 1, '十六', NULL, NULL, 1, '18:00 前', NULL, NULL, NULL, NULL, '石榴大酒店', '2026-09-18 15:44:21', '2026-09-18 15:44:21');
INSERT INTO `tb_order` VALUES (14, 'HT1789718796601712', 2, 5, 1, 'hotel', 'hotel', 40, '已完成', 384.00, 354.00, NULL, NULL, NULL, NULL, 30.00, 3, 'wallet', '2026-09-18 16:06:38', '2026-09-18 16:36:37', '2026-09-18 16:06:57', '212393', '十六', '18892020826', '十六测试支付成功到评价', '{\"hotelId\":5,\"hotelName\":\"石榴大酒店\",\"hotelAddress\":\"奥尊游泳朗陈20楼\",\"hotelPhone\":\"18892020826\",\"roomId\":10,\"roomName\":\"标准大床房\",\"roomSpec\":\"30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物\",\"basePrice\":128.00,\"checkInDate\":\"2026-09-18\",\"checkOutDate\":\"2026-09-19\",\"nights\":1,\"roomCount\":3,\"dailyPrices\":[{\"date\":\"2026-09-18\",\"unitPrice\":128.00}],\"totalAmount\":384.00,\"payAmount\":384.00}', '{\"policyCode\":\"free_before_checkin_18\",\"freeCancelHour\":18,\"summary\":\"入住日18:00前可免费取消\",\"holdUntilHour\":18}', '2026-09-18', '2026-09-19', 1, 3, '十六', NULL, NULL, 1, '18:00 前', NULL, NULL, NULL, NULL, '石榴大酒店', '2026-09-18 16:06:37', '2026-09-18 16:06:37');
INSERT INTO `tb_order` VALUES (15, 'HT1789867689344245', 2, 5, 1, 'hotel', 'hotel', 40, '已入住', 128.00, 128.00, NULL, NULL, NULL, NULL, 0.00, NULL, 'wallet', '2026-09-20 09:28:11', '2026-09-20 09:58:09', '2026-09-20 09:28:32', '965554', '十六', '18892020826', '撒大大', '{\"hotelId\":5,\"hotelName\":\"石榴大酒店\",\"hotelAddress\":\"奥尊游泳朗陈20楼\",\"hotelPhone\":\"18892020826\",\"roomId\":10,\"roomName\":\"标准大床房\",\"roomSpec\":\"30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物\",\"basePrice\":128.00,\"checkInDate\":\"2026-09-20\",\"checkOutDate\":\"2026-09-21\",\"nights\":1,\"roomCount\":1,\"dailyPrices\":[{\"date\":\"2026-09-20\",\"unitPrice\":128.00}],\"totalAmount\":128.00,\"payAmount\":128.00}', '{\"policyCode\":\"free_before_checkin_18\",\"freeCancelHour\":18,\"summary\":\"入住日18:00前可免费取消\",\"holdUntilHour\":18}', '2026-09-20', '2026-09-21', 1, 1, '十六', NULL, NULL, 1, '18:00 前', NULL, NULL, NULL, NULL, '石榴大酒店', '2026-09-20 09:28:09', '2026-09-20 09:50:53');
INSERT INTO `tb_order` VALUES (16, 'RT1790043693213814', 2, 6, 1, 'route', 'route', 40, '已完成', 2998.00, 0.00, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, '十六', '18892020826', '十六的旅游路线预约', NULL, NULL, NULL, NULL, NULL, 1, '十六', NULL, '2026-09-26', 1, NULL, '乌鲁木齐市', NULL, NULL, NULL, '探秘南疆11日全景游！坐飞机玩南疆，2999元/人，南疆自然风光+人文一次体验', '2026-09-22 10:21:33', '2026-09-22 10:21:33');
INSERT INTO `tb_order` VALUES (17, 'RT1790131563650207', 2, 6, 1, 'route', 'route', 50, '已取消', 16197.00, 16187.00, 16187.00, 0.00, 0.00, NULL, 10.00, 4, 'wallet', '2026-09-23 11:02:30', '2026-09-23 11:16:04', NULL, '982294', '十六', '18892020826', '一个预定人，两个同行人，使用优惠券；商户天气原因全额退款', NULL, '{\"summary\":\"1. 配置比例为0：可免费取消，全额退款。\\n2. 配置比例大于0：退还实付的对应比例。\\n3. 已核销订单不支持在线退款。\",\"beforeTripRatio\":0,\"onTripDayRatio\":0,\"afterTripRatio\":0,\"meetHoldHour\":10}', NULL, NULL, NULL, 1, '十六、十七、十八', '[{\"idCard\":\"610216199501252154\",\"name\":\"十六\",\"phone\":\"18892020826\",\"role\":\"booker\"},{\"idCard\":\"610526199632521521\",\"name\":\"十七\",\"role\":\"companion\"},{\"idCard\":\"610526199802512541\",\"name\":\"十八\",\"role\":\"companion\"}]', '2026-09-26', 3, NULL, '阿勒泰', NULL, NULL, NULL, '十七旅行社', '2026-09-23 10:46:04', '2026-09-23 11:02:29');
INSERT INTO `tb_order` VALUES (18, 'RT1790133846842435', 2, 6, 1, 'route', 'route', 20, '待出行', 10798.00, 10798.00, NULL, NULL, NULL, NULL, 0.00, NULL, 'wallet', '2026-09-23 11:24:08', '2026-09-23 11:54:07', NULL, '777908', '十六', '18892020826', '测试超时退款', NULL, '{\"summary\":\"1. 配置比例为0：可免费取消，全额退款。\\n2. 配置比例大于0：退还实付的对应比例。\\n3. 已核销订单不支持在线退款。\",\"beforeTripRatio\":0,\"onTripDayRatio\":0,\"afterTripRatio\":0,\"meetHoldHour\":10}', NULL, NULL, NULL, 1, '十六、十七', '[{\"idCard\":\"610526333215251252\",\"name\":\"十六\",\"phone\":\"18892020826\",\"role\":\"booker\"},{\"idCard\":\"610256322152125252\",\"name\":\"十七\",\"role\":\"companion\"}]', '2026-09-23', 2, NULL, '阿勒泰', NULL, NULL, NULL, '十七旅行社', '2026-09-23 11:24:07', '2026-09-23 11:24:07');
INSERT INTO `tb_order` VALUES (19, 'RT202609231547450001', 2, 7, 1, 'restaurant', 'restaurant', 10, 'pending_confirm', 0.00, 0.00, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, '十六', '18892020826', 'canteen:CR202609230930402991', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, '2026-09-23', 2, NULL, NULL, NULL, NULL, NULL, '十八陕菜馆', '2026-09-23 09:30:40', '2026-09-23 15:47:45');
INSERT INTO `tb_order` VALUES (20, 'RT202609231547450003', 3, 7, 1, 'restaurant', 'restaurant', 10, 'pending_confirm', 0.00, 0.00, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, '十七', '18892020827', 'canteen:CR202609231433185667', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, '2026-09-23', 2, NULL, NULL, NULL, NULL, NULL, '十八陕菜馆', '2026-09-23 14:33:19', '2026-09-23 15:47:45');
INSERT INTO `tb_order` VALUES (21, 'RT202609231547450004', 2, 7, 1, 'restaurant', 'restaurant', 10, 'pending_confirm', 0.00, 0.00, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, '测试用户', '18892020826', 'canteen:CR202609231523585366', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, '2026-09-25', 2, NULL, NULL, NULL, NULL, NULL, '十八陕菜馆', '2026-09-23 15:23:59', '2026-09-23 15:47:45');
INSERT INTO `tb_order` VALUES (22, 'RT202609231547450005', 2, 7, 1, 'restaurant', 'restaurant', 10, 'pending_confirm', 0.00, 0.00, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, '排查测试A', '18892020826', 'canteen:CR202609231526355180', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, '2026-09-26', 2, NULL, NULL, NULL, NULL, NULL, '十八陕菜馆', '2026-09-23 15:26:35', '2026-09-23 15:47:45');
INSERT INTO `tb_order` VALUES (23, 'RT202609231547450006', 2, 7, 1, 'restaurant', 'restaurant', 20, '备餐中', 0.00, 0.00, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, '排查测试B', '18892020826', 'canteen:CR202609231526354618', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, '2026-09-26', 2, NULL, NULL, NULL, NULL, NULL, '十八陕菜馆', '2026-09-23 15:26:35', '2026-09-23 15:47:45');
INSERT INTO `tb_order` VALUES (26, 'RT202609231620528995', 3, 7, 4, 'restaurant', 'restaurant', 40, '已到店', 0.00, 0.00, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, '叁拾', '18892020827', '餐饮预约单号：CR202609231620525572；预约时间：12:00', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, '2026-09-23', 2, NULL, NULL, NULL, NULL, NULL, '十八陕菜馆', '2026-09-23 16:20:53', '2026-09-23 16:20:52');
INSERT INTO `tb_order` VALUES (27, 'OD1790227052165352', 4, 8, 1, 'culture', 'culture', 40, '已完成', 23.90, 23.90, NULL, 23.90, 0.00, '2026-09-24 16:11:33', 0.00, NULL, 'wallet', '2026-09-24 13:17:34', '2026-09-24 13:47:32', NULL, NULL, '十八', '18892020828', NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, '十八', '18892020828', '陕西省西安市新城区永兴坊', '叁拾文创专卖店', '2026-09-24 13:17:32', '2026-09-24 16:11:33');
INSERT INTO `tb_order` VALUES (28, 'OD1790227447374118', 4, 8, 1, 'culture', 'culture', 50, '已取消', 33.90, 33.90, NULL, NULL, NULL, NULL, 0.00, NULL, 'wallet', '2026-09-24 13:24:08', '2026-09-24 13:54:07', NULL, NULL, '十八', '18892020828', '物流信息：顺丰 SF123456；退款已完成', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, '十八', '18892020828', '陕西省西安市新城区永兴坊', '叁拾文创专卖店', '2026-09-24 13:24:07', '2026-09-24 13:24:08');
INSERT INTO `tb_order` VALUES (29, 'OD1790234904661588', 4, 8, 1, 'culture', 'culture', 60, '退款中', 33.90, 33.90, NULL, NULL, NULL, NULL, 0.00, NULL, 'wallet', '2026-09-24 15:28:27', '2026-09-24 15:58:25', NULL, NULL, '十八', '18892020828', NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, '十八', '18892020828', '陕西省西安市新城区永兴坊', '叁拾文创专卖店', '2026-09-24 15:28:25', '2026-09-24 15:28:26');
INSERT INTO `tb_order` VALUES (30, 'OD1790234942539774', 4, 8, 1, 'culture', 'culture', 20, '待发货', 39.80, 39.80, NULL, NULL, NULL, NULL, 0.00, NULL, 'wallet', '2026-09-24 15:29:04', '2026-09-24 15:59:03', NULL, NULL, '十八', '18892020828', NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, '十八', '18892020828', '陕西省西安市新城区永兴坊', '叁拾文创专卖店', '2026-09-24 15:29:03', '2026-09-24 15:29:03');
INSERT INTO `tb_order` VALUES (31, 'OD1790236304667593', 2, 8, 1, 'culture', 'culture', 60, '退款中', 33.90, 33.90, NULL, NULL, NULL, NULL, 0.00, NULL, 'wallet', '2026-09-24 15:51:46', '2026-09-24 16:21:45', NULL, NULL, '十六', '18892020826', NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, '十六', '18892020826', '陕西省西安市新城区东一路', '叁拾文创专卖店', '2026-09-24 15:51:45', '2026-09-24 15:51:45');
INSERT INTO `tb_order` VALUES (32, 'OD1790236377798864', 2, 8, 1, 'culture', 'culture', 20, '待发货', 33.90, 33.90, NULL, NULL, NULL, NULL, 0.00, NULL, 'wallet', '2026-09-24 15:52:59', '2026-09-24 16:22:58', NULL, NULL, '十六', '18892020826', NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, '十六', '18892020826', '陕西省西安市新城区东一路', '叁拾文创专卖店', '2026-09-24 15:52:58', '2026-09-24 15:52:58');
INSERT INTO `tb_order` VALUES (33, 'OD1790236387743856', 2, 8, 1, 'culture', 'culture', 50, '已取消', 23.90, 23.90, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, '2026-09-24 16:23:08', NULL, NULL, '十六', '18892020826', '系统自动取消：支付超时', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, '十六', '18892020826', '陕西省西安市新城区东一路', '叁拾文创专卖店', '2026-09-24 15:53:08', '2026-09-24 15:53:07');
INSERT INTO `tb_order` VALUES (34, 'OD1790238773837498', 2, 8, 1, 'culture', 'culture', 0, '待支付', 23.90, 23.90, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, '2026-09-24 17:02:54', NULL, NULL, '十六', '18892020826', NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, '十六', '18892020826', '陕西省西安市新城区东一路', '叁拾文创专卖店', '2026-09-24 16:32:54', '2026-09-24 16:32:53');
INSERT INTO `tb_order` VALUES (35, 'OD1790239574786296', 2, 8, 1, 'culture', 'culture', 0, '待支付', 23.90, 23.90, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, '2026-09-24 17:16:15', NULL, NULL, '十六', '18892020826', NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, '十六', '18892020826', '陕西省西安市新城区东一路', '叁拾文创专卖店', '2026-09-24 16:46:15', '2026-09-24 16:46:14');

-- ----------------------------
-- Table structure for tb_order_item
-- ----------------------------
DROP TABLE IF EXISTS `tb_order_item`;
CREATE TABLE `tb_order_item`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint(0) NOT NULL COMMENT '订单ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号',
  `item_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '明细类型：room/route/goods',
  `item_id` bigint(0) NULL DEFAULT NULL COMMENT '关联房型/线路/商品ID',
  `item_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '明细名称',
  `item_spec` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规格描述',
  `cover_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图',
  `quantity` int(0) NOT NULL DEFAULT 1 COMMENT '数量',
  `unit_price` decimal(12, 2) NOT NULL COMMENT '单价',
  `total_price` decimal(12, 2) NOT NULL COMMENT '小计',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_item_order_id`(`order_id`) USING BTREE,
  INDEX `idx_order_item_order_no`(`order_no`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_order_item
-- ----------------------------
INSERT INTO `tb_order_item` VALUES (1, 1, 'RT20260908001', 'route', NULL, '荣昌一日游·古镇非遗', '2026-09-15 · 2人', NULL, 1, 198.00, 198.00, '2026-09-10 09:43:33', '2026-09-10 09:43:33');
INSERT INTO `tb_order_item` VALUES (2, 2, 'RT20260907002', 'route', NULL, '荣昌一日游·古镇非遗', '2026-09-12 · 4人', NULL, 2, 198.00, 396.00, '2026-09-10 09:43:33', '2026-09-10 09:43:33');
INSERT INTO `tb_order_item` VALUES (3, 3, 'HT1789638031264709', 'room', 10, '标准大床房', '30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物', 'https://tu.mianyazuju.com/2026/09/17/7d4bb40c-2520-48c2-994a-3bf84e005a55.png', 1, 128.00, 128.00, '2026-09-17 17:40:31', '2026-09-17 17:40:31');
INSERT INTO `tb_order_item` VALUES (4, 4, 'HT1789639253362815', 'room', 10, '标准大床房', '30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物', 'https://tu.mianyazuju.com/2026/09/17/7d4bb40c-2520-48c2-994a-3bf84e005a55.png', 1, 128.00, 128.00, '2026-09-17 18:00:53', '2026-09-17 18:00:53');
INSERT INTO `tb_order_item` VALUES (5, 5, 'HT1789639481728115', 'room', 10, '标准大床房', '30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物', 'https://tu.mianyazuju.com/2026/09/17/7d4bb40c-2520-48c2-994a-3bf84e005a55.png', 1, 128.00, 128.00, '2026-09-17 18:04:42', '2026-09-17 18:04:41');
INSERT INTO `tb_order_item` VALUES (6, 6, 'HT1789701510116387', 'room', 10, '标准大床房', '30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物', 'https://tu.mianyazuju.com/2026/09/17/7d4bb40c-2520-48c2-994a-3bf84e005a55.png', 1, 128.00, 128.00, '2026-09-18 11:18:30', '2026-09-18 11:18:30');
INSERT INTO `tb_order_item` VALUES (7, 7, 'HT1789704334238915', 'room', 10, '标准大床房', '30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物', 'https://tu.mianyazuju.com/2026/09/17/7d4bb40c-2520-48c2-994a-3bf84e005a55.png', 1, 128.00, 128.00, '2026-09-18 12:05:34', '2026-09-18 12:05:34');
INSERT INTO `tb_order_item` VALUES (8, 8, 'HT1789704334409472', 'room', 10, '标准大床房', '30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物', 'https://tu.mianyazuju.com/2026/09/17/7d4bb40c-2520-48c2-994a-3bf84e005a55.png', 1, 128.00, 128.00, '2026-09-18 12:05:34', '2026-09-18 12:05:34');
INSERT INTO `tb_order_item` VALUES (9, 9, 'HT1789704334658237', 'room', 10, '标准大床房', '30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物', 'https://tu.mianyazuju.com/2026/09/17/7d4bb40c-2520-48c2-994a-3bf84e005a55.png', 1, 128.00, 256.00, '2026-09-18 12:05:35', '2026-09-18 12:05:34');
INSERT INTO `tb_order_item` VALUES (10, 10, 'HT1789704334832832', 'room', 10, '标准大床房', '30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物', 'https://tu.mianyazuju.com/2026/09/17/7d4bb40c-2520-48c2-994a-3bf84e005a55.png', 1, 128.00, 128.00, '2026-09-18 12:05:35', '2026-09-18 12:05:34');
INSERT INTO `tb_order_item` VALUES (11, 11, 'HT1789712348595767', 'room', 10, '标准大床房', '30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物', 'https://tu.mianyazuju.com/2026/09/17/7d4bb40c-2520-48c2-994a-3bf84e005a55.png', 1, 128.00, 128.00, '2026-09-18 14:19:09', '2026-09-18 14:19:08');
INSERT INTO `tb_order_item` VALUES (12, 12, 'HT1789714105261153', 'room', 10, '标准大床房', '30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物', 'https://tu.mianyazuju.com/2026/09/17/7d4bb40c-2520-48c2-994a-3bf84e005a55.png', 1, 128.00, 128.00, '2026-09-18 14:48:25', '2026-09-18 14:48:25');
INSERT INTO `tb_order_item` VALUES (13, 13, 'HT1789717461338462', 'room', 10, '标准大床房', '30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物', 'https://tu.mianyazuju.com/2026/09/17/7d4bb40c-2520-48c2-994a-3bf84e005a55.png', 1, 128.00, 128.00, '2026-09-18 15:44:21', '2026-09-18 15:44:21');
INSERT INTO `tb_order_item` VALUES (14, 14, 'HT1789718796601712', 'room', 10, '标准大床房', '30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物', 'https://tu.mianyazuju.com/2026/09/17/7d4bb40c-2520-48c2-994a-3bf84e005a55.png', 1, 128.00, 384.00, '2026-09-18 16:06:37', '2026-09-18 16:06:36');
INSERT INTO `tb_order_item` VALUES (15, 15, 'HT1789867689344245', 'room', 10, '标准大床房', '30㎡ · 2人 · 部分有窗 · 可吸烟 · 1张大床1.8米 · 不可携带宠物', 'https://tu.mianyazuju.com/2026/09/17/7d4bb40c-2520-48c2-994a-3bf84e005a55.png', 1, 128.00, 128.00, '2026-09-20 09:28:09', '2026-09-20 09:28:09');
INSERT INTO `tb_order_item` VALUES (16, 16, 'RT1790043693213814', 'route', 6, '探秘南疆11日全景游！坐飞机玩南疆，2999元/人，南疆自然风光+人文一次体验', '3天', 'https://tu.mianyazuju.com/2026/09/21/d59104b4-5316-4c53-bc58-22c8b7fa105f.jpg', 1, 2998.00, 2998.00, '2026-09-22 10:21:33', '2026-09-22 10:21:33');
INSERT INTO `tb_order_item` VALUES (17, 17, 'RT1790131563650207', 'route', 7, '阿勒泰醉秋，【阿勒泰起止】6日纯玩小团，金秋限定，大自然调色盘！我的阿勒泰，三景区连住连玩', '5天', 'https://tu.mianyazuju.com/2026/09/21/7fc1aae1-1137-4232-842a-03fd96377947.jpg', 3, 5399.00, 16197.00, '2026-09-23 10:46:04', '2026-09-23 10:46:03');
INSERT INTO `tb_order_item` VALUES (18, 18, 'RT1790133846842435', 'route', 7, '阿勒泰醉秋，【阿勒泰起止】6日纯玩小团，金秋限定，大自然调色盘！我的阿勒泰，三景区连住连玩', '5天', 'https://tu.mianyazuju.com/2026/09/21/7fc1aae1-1137-4232-842a-03fd96377947.jpg', 2, 5399.00, 10798.00, '2026-09-23 11:24:07', '2026-09-23 11:24:06');
INSERT INTO `tb_order_item` VALUES (19, 19, 'RT202609231547450001', 'restaurant', NULL, '十八陕菜馆 booking', '2026-09-23 12:00 x2', NULL, 1, 0.00, 0.00, '2026-09-23 09:30:40', '2026-09-23 15:47:45');
INSERT INTO `tb_order_item` VALUES (20, 20, 'RT202609231547450003', 'restaurant', NULL, '十八陕菜馆 booking', '2026-09-23 12:00 x2', NULL, 1, 0.00, 0.00, '2026-09-23 14:33:19', '2026-09-23 15:47:45');
INSERT INTO `tb_order_item` VALUES (21, 21, 'RT202609231547450004', 'restaurant', NULL, '十八陕菜馆 booking', '2026-09-25 18:00 x2', NULL, 1, 0.00, 0.00, '2026-09-23 15:23:59', '2026-09-23 15:47:45');
INSERT INTO `tb_order_item` VALUES (22, 22, 'RT202609231547450005', 'restaurant', NULL, '十八陕菜馆 booking', '2026-09-26 12:00 x2', NULL, 1, 0.00, 0.00, '2026-09-23 15:26:35', '2026-09-23 15:47:45');
INSERT INTO `tb_order_item` VALUES (23, 23, 'RT202609231547450006', 'restaurant', NULL, '十八陕菜馆 booking', '2026-09-26 18:00 x2', NULL, 1, 0.00, 0.00, '2026-09-23 15:26:35', '2026-09-23 15:47:45');
INSERT INTO `tb_order_item` VALUES (26, 26, 'RT202609231620528995', 'restaurant', NULL, '十八陕菜馆 · 餐饮预约', '2026-09-23 12:00 · 2人', NULL, 1, 0.00, 0.00, '2026-09-23 16:20:53', '2026-09-23 16:20:52');
INSERT INTO `tb_order_item` VALUES (27, 27, 'OD1790227052165352', 'goods', 1, '西安毛绒玩具文创', '大雁塔毛绒文创', 'https://tu.mianyazuju.com/2026/09/23/57b83633-34e9-476c-bde9-c0e0e1b5d249.png', 1, 15.90, 15.90, '2026-09-24 13:17:32', '2026-09-24 13:17:32');
INSERT INTO `tb_order_item` VALUES (28, 28, 'OD1790227447374118', 'goods', 1, '西安毛绒玩具文创', '秦岭大熊猫文创', 'https://tu.mianyazuju.com/2026/09/23/57b83633-34e9-476c-bde9-c0e0e1b5d249.png', 1, 25.90, 25.90, '2026-09-24 13:24:07', '2026-09-24 13:24:07');
INSERT INTO `tb_order_item` VALUES (29, 29, 'OD1790234904661588', 'goods', 1, '西安毛绒玩具文创', '秦岭大熊猫文创', 'https://tu.mianyazuju.com/2026/09/23/57b83633-34e9-476c-bde9-c0e0e1b5d249.png', 1, 25.90, 25.90, '2026-09-24 15:28:25', '2026-09-24 15:28:24');
INSERT INTO `tb_order_item` VALUES (30, 30, 'OD1790234942539774', 'goods', 1, '西安毛绒玩具文创', '大雁塔毛绒文创', 'https://tu.mianyazuju.com/2026/09/23/57b83633-34e9-476c-bde9-c0e0e1b5d249.png', 2, 15.90, 31.80, '2026-09-24 15:29:03', '2026-09-24 15:29:02');
INSERT INTO `tb_order_item` VALUES (31, 31, 'OD1790236304667593', 'goods', 1, '西安毛绒玩具文创', '秦岭大熊猫文创', 'https://tu.mianyazuju.com/2026/09/23/57b83633-34e9-476c-bde9-c0e0e1b5d249.png', 1, 25.90, 25.90, '2026-09-24 15:51:45', '2026-09-24 15:51:44');
INSERT INTO `tb_order_item` VALUES (32, 32, 'OD1790236377798864', 'goods', 1, '西安毛绒玩具文创', '秦岭大熊猫文创', 'https://tu.mianyazuju.com/2026/09/23/57b83633-34e9-476c-bde9-c0e0e1b5d249.png', 1, 25.90, 25.90, '2026-09-24 15:52:58', '2026-09-24 15:52:57');
INSERT INTO `tb_order_item` VALUES (33, 33, 'OD1790236387743856', 'goods', 1, '西安毛绒玩具文创', '大雁塔毛绒文创', 'https://tu.mianyazuju.com/2026/09/23/57b83633-34e9-476c-bde9-c0e0e1b5d249.png', 1, 15.90, 15.90, '2026-09-24 15:53:08', '2026-09-24 15:53:07');
INSERT INTO `tb_order_item` VALUES (34, 34, 'OD1790238773837498', 'goods', 1, '西安毛绒玩具文创', '大雁塔毛绒文创', 'https://tu.mianyazuju.com/2026/09/23/57b83633-34e9-476c-bde9-c0e0e1b5d249.png', 1, 15.90, 15.90, '2026-09-24 16:32:54', '2026-09-24 16:32:53');
INSERT INTO `tb_order_item` VALUES (35, 35, 'OD1790239574786296', 'goods', 1, '西安毛绒玩具文创', '大雁塔毛绒文创', 'https://tu.mianyazuju.com/2026/09/23/57b83633-34e9-476c-bde9-c0e0e1b5d249.png', 1, 15.90, 15.90, '2026-09-24 16:46:15', '2026-09-24 16:46:14');

-- ----------------------------
-- Table structure for tb_route
-- ----------------------------
DROP TABLE IF EXISTS `tb_route`;
CREATE TABLE `tb_route`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id` bigint(0) NOT NULL COMMENT '商户ID',
  `merchant_service_id` bigint(0) NULL DEFAULT NULL COMMENT '关联商户服务ID',
  `destination_id` bigint(0) NOT NULL COMMENT '目的地ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '线路名称',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '短描述',
  `intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '详细介绍',
  `price_from` decimal(12, 2) NOT NULL COMMENT '起步价/人',
  `duration` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '行程时长',
  `day_count` int(0) NULL DEFAULT NULL COMMENT '????',
  `meet_point` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '集合地点',
  `travel_city` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `spots` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '途经景点（逗号分隔）',
  `tags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '标签JSON',
  `cover_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图',
  `banner_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '?????SON???',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_route_merchant_service`(`merchant_service_id`) USING BTREE,
  INDEX `idx_route_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_route_destination_id`(`destination_id`) USING BTREE,
  INDEX `idx_route_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '旅游线路' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_route
-- ----------------------------
INSERT INTO `tb_route` VALUES (6, 6, 15, 4, '探秘南疆11日全景游！坐飞机玩南疆，2999元/人，南疆自然风光+人文一次体验', '喀什古城、博斯腾湖、沙漠公路、巴楚红海胡杨林、盘龙古道、温宿大峡谷、帕米尔高原、喀拉库勒湖、莎车王陵、罗布人村寨沿途风景优美', '喀什古城、博斯腾湖、沙漠公路、巴楚红海胡杨林、盘龙古道、温宿大峡谷、帕米尔高原、喀拉库勒湖、莎车王陵、罗布人村寨沿途风景优美', 2998.00, '3天', 3, '乌鲁木齐市', '乌鲁木齐市', '喀什古城,博斯腾湖,沙漠公路,巴楚红海胡杨林,盘龙古道,温宿大峡谷,帕米尔高原,喀拉库勒湖,莎车王陵,罗布人村寨', '[\"精品小团\",\"含讲解\",\"多回头客\"]', 'https://tu.mianyazuju.com/2026/09/21/d59104b4-5316-4c53-bc58-22c8b7fa105f.jpg', '[\"https://tu.mianyazuju.com/2026/09/21/408006d0-b718-4a57-9ee3-e520ef6ea3e6.jpg\",\"https://tu.mianyazuju.com/2026/09/21/3335bd40-75d3-4bc5-b38a-16515105a435.jpeg\",\"https://tu.mianyazuju.com/2026/09/21/3895d814-d195-4491-9c00-7627d36761e1.jpg\"]', 1, 0, '2026-09-21 17:50:48', '2026-09-21 18:49:20');
INSERT INTO `tb_route` VALUES (7, 6, 16, 4, '阿勒泰醉秋，【阿勒泰起止】6日纯玩小团，金秋限定，大自然调色盘！我的阿勒泰，三景区连住连玩', '喀纳斯、禾木、白哈巴、阿勒泰喀纳斯、禾木、白哈巴、阿勒泰行程天数6天', '喀纳斯、禾木、白哈巴、阿勒泰喀纳斯、禾木、白哈巴、阿勒泰行程天数6天', 5399.00, '5天', 5, '阿勒泰', '阿勒泰地区', '喀纳斯,禾木,白哈巴,阿勒泰', '[\"精品小团\",\"含讲解\",\"多回头客\"]', 'https://tu.mianyazuju.com/2026/09/23/7d39fb59-3db2-456d-9175-2f4bc40e13a4.jpg', '[\"https://tu.mianyazuju.com/2026/09/23/1d9de0a1-61e3-422e-af79-9d9b77592e73.jpg\",\"https://tu.mianyazuju.com/2026/09/23/5b312731-e8e3-4bc8-bc3d-17090dbd4411.jpg\",\"https://tu.mianyazuju.com/2026/09/23/c4f881ae-a59f-4bc3-8d6c-15941c485764.jpg\",\"https://tu.mianyazuju.com/2026/09/23/a7475ef6-77ba-43c8-aad7-a8bbb20182ef.png\"]', 1, 0, '2026-09-22 13:35:03', '2026-09-22 13:35:03');

-- ----------------------------
-- Table structure for tb_route_itinerary
-- ----------------------------
DROP TABLE IF EXISTS `tb_route_itinerary`;
CREATE TABLE `tb_route_itinerary`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `route_id` bigint(0) NOT NULL COMMENT '线路ID',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '节点顺序',
  `day_no` int(0) NULL DEFAULT NULL COMMENT '???????',
  `time_label` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '时间节点，如 09:00',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '节点名称',
  `location` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地点',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '节点描述',
  `cover_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图',
  `images_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '????JSON??',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_route_itinerary_route_id`(`route_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '线路行程节点' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_route_itinerary
-- ----------------------------
INSERT INTO `tb_route_itinerary` VALUES (25, 6, 0, 1, '第1天', '各地--乌鲁木齐（飞机/火车）', NULL, '今日体验：逛吃国际大巴扎 红山公园 参观自治区博物馆\n详细行程：欢迎来到大美新疆！ 免费接机后送您入住酒店休息，然后自由活动，次日出发时间司机和管家会发信息或电话告知，请您注意查收。\n1、新疆与北京等内地城市有2小时时差，执行北京时间；\n2、我社出团前会以短信或来电的方式告知您接机方式，请注意保持手机畅通；\n3、乌鲁木齐距离市区至机场30公里，行车时间在40分钟左右；\n4、早到攻略：可以自行前往大巴扎，和田街，自治区博物馆（提前预约，周一闭馆）', 'https://tu.mianyazuju.com/2026/09/21/635177a1-068d-407b-b532-0d5a40afa349.jpg', '[\"https://tu.mianyazuju.com/2026/09/21/635177a1-068d-407b-b532-0d5a40afa349.jpg\",\"https://tu.mianyazuju.com/2026/09/21/4e37eb08-11df-4aae-8add-e6d256835e8f.jpg\",\"https://tu.mianyazuju.com/2026/09/21/fae0a81a-eade-4ab3-adbd-4a4cbcd10a45.png\",\"https://tu.mianyazuju.com/2026/09/21/9f0e6e93-b8fb-423e-a004-160e1c5d2ccc.jpg\"]', '2026-09-21 18:39:24', '2026-09-21 18:39:23');
INSERT INTO `tb_route_itinerary` VALUES (26, 6, 1, 2, '第2天', '乌鲁木齐-乌尉高速(天山胜利隧道)-罗布人村寨-库尔勒（单程约500公里，行车时间约7小时）', NULL, '早餐后，乘车出发沿乌尉高速驶入天山胜利隧道，隧道内通风良好、照明充足，采用 “三洞 + 四竖井” 设计，仅需 20 分钟即可穿越天山，相比传统路线缩短 3.5 小时车程。感受穿越天山的壮阔。随后抵达罗布人村寨，这里是沙漠与绿洲交汇的秘境；\n傍晚抵达库尔勒，入住酒店，可自由前往孔雀河畔散步，欣赏库尔勒夜景。', 'https://tu.mianyazuju.com/2026/09/21/f172c1dc-fafb-496b-9802-8c54dadea9ad.jpg', '[\"https://tu.mianyazuju.com/2026/09/21/f172c1dc-fafb-496b-9802-8c54dadea9ad.jpg\",\"https://tu.mianyazuju.com/2026/09/21/ceddebed-6897-43ae-a0d0-cb4b5cc20bf8.jpg\",\"https://tu.mianyazuju.com/2026/09/21/cb719f9b-a897-4997-969b-176cec082acc.jpg\",\"https://tu.mianyazuju.com/2026/09/21/8b1f763e-c884-4c49-a9dd-d655edce665e.jpg\"]', '2026-09-21 18:39:24', '2026-09-21 18:39:23');
INSERT INTO `tb_route_itinerary` VALUES (27, 6, 2, 3, '第3天', '库尔勒--温宿大峡谷--阿克苏（单程约595公里，行车时间约8小时）', NULL, '早餐后，乘车前往游览【温宿大峡谷】又叫温宿托尔木大峡谷，温宿大峡谷有中国罕见的远古岩盐地质绝景、中国西部奇特的雅丹地质怪景、中国独有的巨型岩溶蚀地质秘境，堪称新疆“活的地质演变史博物馆”，进入景区，犹如进入了一片精美雄浑的自然画廊。 峡谷中山壁岩层分布清晰，受挤压形成的褶皱，弯曲的线条十分清晰，断裂的岩石夹在山壁岩层中， 在历经亿万年的风雨侵蚀后，形成了绝壁高耸、奇峰兀立、形态各异、嶙峋怪异、色彩浓烈、千姿百 态、五彩纷呈的奇特景观。', 'https://tu.mianyazuju.com/2026/09/21/5fd5906a-9337-4638-8c62-c2217b096275.jpg', '[\"https://tu.mianyazuju.com/2026/09/21/5fd5906a-9337-4638-8c62-c2217b096275.jpg\",\"https://tu.mianyazuju.com/2026/09/21/1e1642e0-3d22-4250-b6d8-cbb5e64f74f3.jpg\",\"https://tu.mianyazuju.com/2026/09/21/3f49684a-7a36-481f-bc8a-95060c619b0a.jpg\"]', '2026-09-21 18:39:24', '2026-09-21 18:39:23');
INSERT INTO `tb_route_itinerary` VALUES (33, 7, 0, 1, '第1天', '阿勒泰接机/火车', NULL, '今日主题——准时接机/接站，送到指定酒店\n推荐活动：逛克兰河，感受北疆初秋\n温馨提示：\n新疆与北京等内地城市有2小时时差，执行北京时间；\n我社出团前会以短信或来电的方式告知您接机方式，请注意保持手机畅通；\n阿勒泰机场至市区约30公里，行车时间在40分钟左右；\n秋季阿勒泰早晚温差大，请带好保暖外套。', 'https://tu.mianyazuju.com/2026/09/22/d5f41314-3d65-45d5-99c0-bc1331ab875e.jpg', '[\"https://tu.mianyazuju.com/2026/09/22/d5f41314-3d65-45d5-99c0-bc1331ab875e.jpg\",\"https://tu.mianyazuju.com/2026/09/22/c5e92653-6814-428e-8f0a-b9c47ca33db9.jpg\",\"https://tu.mianyazuju.com/2026/09/22/3ea1cba7-6565-41c6-bdf8-65c2f7135583.jpg\",\"https://tu.mianyazuju.com/2026/09/22/60809b17-3df9-48be-931e-d3a142cdfa94.png\"]', '2026-09-23 13:32:56', '2026-09-23 13:32:56');
INSERT INTO `tb_route_itinerary` VALUES (34, 7, 1, 2, '第2天', '阿勒泰→阿禾公路→禾木（230公里，约5小时）', NULL, '08:00-08:30 酒店早餐\n09:00-13:00 踏上被誉为“景观大道”的阿禾公路（208公里）。沿途阿勒泰山脉层林尽染，白桦林通体金黄，与墨绿的云杉、湛蓝的天空、皑皑雪山构成震撼的色彩交响乐。\n13:30-14:30 抵达【禾木村国家5A级景区】，换乘区间车进入。入住特色小木屋。\n15:00-18:00 漫步禾木村：禾木桥、白桦林、百年老屋。河边的白桦林秋色正浓。\n18:00-20:00 登上哈登观景台，俯瞰整个村庄。（赠送禾木轻旅拍：含6张精修成片 + 底片全送，不含妆造服饰）\n20:00后是拍摄璀璨银河和星空的绝佳地点。', 'https://tu.mianyazuju.com/2026/09/22/8259982b-4089-40f0-a606-972794545b58.jpg', '[\"https://tu.mianyazuju.com/2026/09/22/8259982b-4089-40f0-a606-972794545b58.jpg\",\"https://tu.mianyazuju.com/2026/09/22/d78c96c6-3ae2-4367-82ee-32404d833f83.jpg\",\"https://tu.mianyazuju.com/2026/09/22/c578d32e-c8d9-483d-9347-bd50b8b08f37.jpg\"]', '2026-09-23 13:32:56', '2026-09-23 13:32:56');
INSERT INTO `tb_route_itinerary` VALUES (35, 7, 2, 3, '第3天', '禾木→喀纳斯（120公里，约3小时）', NULL, '06:30-08:00 早餐自理，早起（自愿）：登上观景台欣赏禾木晨雾。\n09:00-09:30 收拾行李，乘坐区间车出村。\n10:00-11:30 前往贾登峪停车场。\n11:30-17:00 换乘区间车游览【喀纳斯国家5A级景区】（含门票+一进区间车）：\n登【观鱼台】（1068级台阶，视体力自选）：站在制高点，将整个喀纳斯湖尽收眼底。湖水如翡翠镶嵌在漫山遍野的金黄、橙红、深绿之中，远眺友谊峰雪顶，探寻“湖怪”传说。\n漫步三湾：神仙湾、月亮湾、卧龙湾。秋色层次分明，色彩斑斓，随手一拍都是大片。\n17:30-18:30 夜宿喀纳斯景。', 'https://tu.mianyazuju.com/2026/09/22/5015a977-605f-4c0d-97c4-2c738cbe88ec.jpg', '[\"https://tu.mianyazuju.com/2026/09/22/5015a977-605f-4c0d-97c4-2c738cbe88ec.jpg\",\"https://tu.mianyazuju.com/2026/09/22/f854dbeb-e8bf-407f-8d89-a4b72cb259ae.jpg\",\"https://tu.mianyazuju.com/2026/09/22/b85a38a4-66b3-49cc-a2f5-e0da43f49696.jpg\"]', '2026-09-23 13:32:56', '2026-09-23 13:32:56');
INSERT INTO `tb_route_itinerary` VALUES (36, 7, 3, 4, '第4天', '喀纳斯→铁贾公路→白哈巴（270公里，约4.5小时）', NULL, '08:00-08:30 早餐自理\n09:00-12:30 吃饱睡足后慢逛喀纳斯，村内及周边遍布金黄色的白桦林，秋季时树叶呈现火红、金黄等色彩，与墨绿的松林形成鲜明对比，构成童话般的油画感。随后乘车前往著名的【神仙湾、卧龙湾、月亮湾】三个打卡点，徒步游三湾。\n12:30-13:30 午餐自理\n14:00-17:30 前往【白哈巴】（含门票+区间车）。沿途途经铁贾公路，两侧森林色彩斑斓，如同一幅巨型的秋日画卷。\n17:30-19:00 白哈巴村庄规模小巧宁静，被五彩斑斓的秋色山林环绕。\n晚宿：白哈巴景区住宿。', 'https://tu.mianyazuju.com/2026/09/22/71857cc9-d161-45f4-9833-345097e7aa69.jpg', '[\"https://tu.mianyazuju.com/2026/09/22/71857cc9-d161-45f4-9833-345097e7aa69.jpg\",\"https://tu.mianyazuju.com/2026/09/22/d217d973-bed9-4959-9251-c6d026c3ee07.jpg\",\"https://tu.mianyazuju.com/2026/09/22/bd15ad73-0750-46a3-a9b5-41ad15b7f9c9.png\"]', '2026-09-23 13:32:57', '2026-09-23 13:32:56');
INSERT INTO `tb_route_itinerary` VALUES (37, 7, 4, 5, '第5天', '白哈巴→阿勒泰（270公里，约4小时）', NULL, '今日主题——在白桦林间漫步，在边境线上凝望\n08:30-09:30 睡到自然醒，早餐自理。\n09:30-12:00 慢逛白哈巴村：村内及周边遍布金黄色的白桦林，秋季时树叶呈现火红、金黄等色彩，与墨绿的松林形成鲜明对比，构成童话般的油画感。中哈边界附近的峡谷两侧植被色彩斑斓，层次分明，是国境线上的巨型自然画卷。\n12:00-13:00 午餐自理\n13:00-17:00 沿G219国道返回阿勒泰。沿途欣赏边境公路的壮阔秋色。\n17:30 抵达阿勒泰，入住携程4钻酒店。晚餐可自由探索阿勒泰市区美食。', 'https://tu.mianyazuju.com/2026/09/22/1b925106-34c6-4728-a94e-1f15c12378e2.jpg', '[\"https://tu.mianyazuju.com/2026/09/22/1b925106-34c6-4728-a94e-1f15c12378e2.jpg\",\"https://tu.mianyazuju.com/2026/09/22/8dac58bb-a98b-438a-a11f-d31b61aec50c.jpg\",\"https://tu.mianyazuju.com/2026/09/22/9f75fa7f-906e-47a1-b03c-e86005272bec.jpg\",\"https://tu.mianyazuju.com/2026/09/22/c91669ad-a476-47e5-8659-ab7d46f25faf.png\"]', '2026-09-23 13:32:57', '2026-09-23 13:32:56');

-- ----------------------------
-- Table structure for tb_route_review
-- ----------------------------
DROP TABLE IF EXISTS `tb_route_review`;
CREATE TABLE `tb_route_review`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `merchant_id` bigint(0) NOT NULL COMMENT 'merchant id',
  `route_id` bigint(0) NOT NULL COMMENT 'route id',
  `order_id` bigint(0) NOT NULL COMMENT 'order id',
  `user_id` bigint(0) NOT NULL COMMENT 'user id',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'user name',
  `content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'content',
  `rating` tinyint(0) NOT NULL DEFAULT 5 COMMENT 'rating 1-5',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'images json',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT 'create time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_route_review_order_user`(`order_id`, `user_id`) USING BTREE,
  INDEX `idx_route_review_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_route_review_route_id`(`route_id`) USING BTREE,
  INDEX `idx_route_review_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'route review' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_route_review
-- ----------------------------

-- ----------------------------
-- Table structure for tb_scenic
-- ----------------------------
DROP TABLE IF EXISTS `tb_scenic`;
CREATE TABLE `tb_scenic`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `destination_id` bigint(0) NOT NULL COMMENT '目的地ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '景区名称',
  `address` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '景区地址',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度',
  `open_time` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '开放时间',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '咨询电话',
  `level_label` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '景区等级，如4A级',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类编码',
  `intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '景区特色介绍',
  `feature_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '特色配图JSON数组',
  `banner_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '轮播图JSON数组',
  `tips` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '游玩提示JSON数组',
  `price_from` decimal(12, 2) NULL DEFAULT NULL COMMENT '门票起步价',
  `tag` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '标签JSON数组',
  `cover_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_scenic_destination_id`(`destination_id`) USING BTREE,
  INDEX `idx_scenic_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '旅游景区' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_scenic
-- ----------------------------
INSERT INTO `tb_scenic` VALUES (5, 4, '秦始皇帝陵博物院（兵马俑）', '陕西省西安市临潼区西杨路', 34.3847310, 109.2805400, '08:30-17:00（旺季至18:30闭园）', '029-81399127', '5A级', 'culture', '由秦始皇兵马俑博物馆与丽山园（秦始皇陵考古遗址公园）组成，1974年发现兵马俑，1987年列入《世界遗产名录》，被誉为\"世界第八大奇迹\"。', '[\"https://tu.mianyazuju.com/2026/09/21/5ef8edbe-c5be-465f-8cbc-6823f4b06abe.jpg\",\"https://tu.mianyazuju.com/2026/09/21/2ef55020-5455-4762-aaf3-dcca977d6870.png\"]', '[\"https://tu.mianyazuju.com/2026/09/21/14fba013-8535-4e9a-af7a-81c4bd2cde2e.jpg\",\"https://tu.mianyazuju.com/2026/09/21/c26ef2bd-714c-47ed-8538-614697592c5f.png\",\"https://tu.mianyazuju.com/2026/09/21/71b87975-21c4-487c-97a9-8bd39c6fdecd.jpg\",\"https://tu.mianyazuju.com/2026/09/21/53420caa-c8f0-4555-b194-8fe228f961b8.jpg\"]', '[\"须通过「秦始皇帝陵博物院」官方公众号实名预约购票，现场不售票\",\"兵马俑与丽山园可凭当日门票免费乘坐摆渡车往返\",\"旺季建议提前7-10天预约\"]', 120.00, '[\"世界遗产\"]', 'https://tu.mianyazuju.com/2026/09/21/1e9bfbee-a4de-4ad1-8437-fcca797533cb.png', 1, 1, '2026-09-10 09:44:16', '2026-09-21 13:29:46');
INSERT INTO `tb_scenic` VALUES (6, 4, '华清宫', '陕西省西安市临潼区华清路38号', 34.3630560, 109.2141670, '07:30-18:00（冬季17:30闭园）', '029-83812004', '5A级', 'culture', '唐代皇家离宫别苑，南依骊山、北临渭河，是唐明皇与杨贵妃爱情故事及1936年西安事变发生地，唐代御汤遗址与骊山五间厅为重要看点。', '[\"https://tu.mianyazuju.com/2026/09/21/434a123c-ff51-4d18-a0cd-255084967a01.webp\",\"https://tu.mianyazuju.com/2026/09/21/8d0a9be1-f088-46a4-8582-1052ba794558.jpg\"]', '[\"https://tu.mianyazuju.com/2026/09/21/40a1d67d-1490-49af-91b3-2dfe920563db.webp\",\"https://tu.mianyazuju.com/2026/09/21/9e82d3d1-a341-4fbc-b03f-2e3e60a5e827.webp\",\"https://tu.mianyazuju.com/2026/09/21/82393e40-0293-498e-9c23-6fee2ea53f1e.webp\"]', '[\"门票含骊山，骊山索道需另购\",\"《长恨歌》演出票另购，与日间门票分开\",\"近地铁9号线华清池站\"]', 120.00, '[\"唐宫温泉\"]', 'https://tu.mianyazuju.com/2026/09/21/a75cc0df-281d-4bbe-9ab9-b6ecc21b6b35.jpg', 1, 2, '2026-09-10 09:44:16', '2026-09-21 13:31:52');
INSERT INTO `tb_scenic` VALUES (7, 4, '陕西历史博物馆', '陕西省西安市雁塔区文娱巷', 34.2247220, 108.9541670, '08:30-19:00（18:00停止检票）', '029-85253806', '国家一级', 'culture', '馆藏文物37万余件，上起远古、下迄1840年前，商周青铜器、历代陶俑、汉唐金银器与唐墓壁画等精品荟萃，被誉为\"华夏宝库\"。', '[\"https://tu.mianyazuju.com/2026/09/21/f402028b-1589-4b1b-8f44-2dbca7d7f0ed.jpg\",\"https://tu.mianyazuju.com/2026/09/21/c519524a-26b3-4aa0-9123-6bb9c1068f10.jpg\"]', '[\"https://tu.mianyazuju.com/2026/09/21/2f7dd57c-e9b6-48be-94c0-775341817025.png\",\"https://tu.mianyazuju.com/2026/09/21/48b5ca1f-dc16-4d07-8a58-cf9caa93bce3.jpg\",\"https://tu.mianyazuju.com/2026/09/21/246b0324-b95c-47f7-a6bf-16c7b4df972e.jpg\",\"https://tu.mianyazuju.com/2026/09/21/1fa1ff1e-6e04-4b22-be4b-6aad165f1656.jpg\"]', '[\"基本陈列免费，须关注「陕西历史博物馆」微信公众号提前预约\",\"实行提前5天放票，本馆每日17:00放票\",\"未授权任何第三方代售门票\"]', 0.00, '[\"免费预约\"]', 'https://tu.mianyazuju.com/2026/09/21/6a166cf3-209d-47b5-a265-411d4ae3a43b.jpg', 1, 3, '2026-09-10 09:44:16', '2026-09-21 13:34:35');
INSERT INTO `tb_scenic` VALUES (8, 4, '大慈恩寺（大雁塔）', '陕西省西安市雁塔区芙蓉东路', 34.2180560, 108.9647220, '08:30-18:00（17:00停止入园）', '029-85519932', '4A级', 'culture', '唐代长安著名皇家寺院，玄奘法师曾在此主持译场并督造大雁塔，为中国佛教唯识宗祖庭，1961年列为全国重点文物保护单位。', '[\"https://tu.mianyazuju.com/2026/09/21/4e59d151-4e99-46b5-abae-d0b100daa42e.jpg\",\"https://tu.mianyazuju.com/2026/09/21/146abf31-fbf2-4fe9-8c54-2780f69c01f4.jpg\"]', '[\"https://tu.mianyazuju.com/2026/09/21/ca366b61-57be-48a5-a5f7-a3bb17b391a6.png\",\"https://tu.mianyazuju.com/2026/09/21/dd0cc37a-e545-4477-b45f-07f264835064.jpg\",\"https://tu.mianyazuju.com/2026/09/21/d2fe96ee-adc1-4c11-9439-710f96b50660.jpg\",\"https://tu.mianyazuju.com/2026/09/21/344c2d40-ac88-44b6-9c23-4698c42e21b6.jpg\"]', '[\"大慈恩寺门票与登塔25元分开购买\",\"大雁塔北广场音乐喷泉免费开放\",\"地铁3/4号线大雁塔站可达\"]', 40.00, '[\"玄奘译经\"]', 'https://tu.mianyazuju.com/2026/09/21/7d138d31-b22a-4e39-ad7d-856f1984a5bf.jpg', 1, 4, '2026-09-10 09:44:16', '2026-09-21 13:36:18');
INSERT INTO `tb_scenic` VALUES (10, 4, '大唐不夜城', '陕西省西安市雁塔区雁塔南路550号', 34.2138890, 108.9647220, '全天24小时开放', '029-81211080', '国家级街区', 'culture', '以大雁塔为背景的盛唐主题步行街，全长2.1公里，夜间灯光与《贞观之治》《唐韵不倒翁》等免费街头演艺集中上演，首批国家级夜间文化和旅游消费集聚区。', '[\"https://tu.mianyazuju.com/2026/09/21/9a376f8d-7ba7-4c11-b342-b2747aeac949.jpg\",\"https://tu.mianyazuju.com/2026/09/21/2252b40e-e7ec-421a-bb6f-8334b6e882ba.jpg\"]', '[\"https://tu.mianyazuju.com/2026/09/21/1da0037f-a4f2-4e27-a070-038eef0a7386.jpg\",\"https://tu.mianyazuju.com/2026/09/21/dd8e7cc1-dde4-4078-aa49-fd57744d6692.jpg\",\"https://tu.mianyazuju.com/2026/09/21/0fc237c0-7ac0-486d-988f-ba1b5973e547.jpg\",\"https://tu.mianyazuju.com/2026/09/21/38cdd1e6-b4bf-4a8c-b9e0-02e14b78e99e.jpg\"]', '[\"街区免费开放，演出多在19:00-22:30\",\"地铁3/4号线大雁塔站或4号线大唐芙蓉园站可达\",\"节假日人流较大，建议错峰\"]', 0.00, '[\"免费街区\"]', 'https://tu.mianyazuju.com/2026/09/21/da1e02e7-54de-4aee-949e-6689d42b320d.jpg', 1, 6, '2026-09-10 09:44:16', '2026-09-21 13:39:27');

-- ----------------------------
-- Table structure for tb_scenic_category
-- ----------------------------
DROP TABLE IF EXISTS `tb_scenic_category`;
CREATE TABLE `tb_scenic_category`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类编码（业务键）',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_scenic_category_code`(`code`) USING BTREE,
  INDEX `idx_scenic_category_status_sort`(`status`, `sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '景区分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_scenic_category
-- ----------------------------
INSERT INTO `tb_scenic_category` VALUES (1, 'culture', '人文古迹', 10, 1, '2026-09-21 13:18:40', '2026-09-21 13:24:50');
INSERT INTO `tb_scenic_category` VALUES (2, 'nature', '自然风光', 20, 1, '2026-09-21 13:18:40', '2026-09-21 13:18:40');
INSERT INTO `tb_scenic_category` VALUES (3, 'red', '红色旅游', 30, 1, '2026-09-21 13:18:40', '2026-09-21 13:18:40');
INSERT INTO `tb_scenic_category` VALUES (4, 'folk', '民俗风情', 40, 1, '2026-09-21 13:18:40', '2026-09-21 13:18:40');

-- ----------------------------
-- Table structure for tb_transport_route
-- ----------------------------
DROP TABLE IF EXISTS `tb_transport_route`;
CREATE TABLE `tb_transport_route`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `route_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '线路业务编码，如 101',
  `destination_id` bigint(0) NULL DEFAULT NULL COMMENT '目的地ID',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度（发布选点，匹配目的地）',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度（发布选点，匹配目的地）',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '线路名称，如 101路',
  `first_bus` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '首班时间',
  `last_bus` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '末班时间',
  `start_station` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '起点站',
  `end_station` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '终点站',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
  `status_key` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'running' COMMENT '运行状态键',
  `status_label` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '运行中' COMMENT '运行状态文案',
  `accent_color` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '主题色',
  `accent_light` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '浅色主题',
  `show_realtime_stops` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否展示实时站点',
  `expandable` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否可展开站点',
  `footer_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '底部类型：expand/location',
  `current_stop_index` int(0) NOT NULL DEFAULT 0 COMMENT '当前站序号',
  `current_stop` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '当前站名称',
  `next_stop` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '下一站名称',
  `next_eta` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '预计到站',
  `directions_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '上下行方向 JSON',
  `track_info_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '实时车辆 JSON',
  `realtime_updates_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '实时动态 JSON',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_transport_route_code`(`route_code`) USING BTREE,
  INDEX `idx_transport_route_destination`(`destination_id`) USING BTREE,
  INDEX `idx_transport_route_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '公交线路' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_transport_route
-- ----------------------------
INSERT INTO `tb_transport_route` VALUES (1, '101', NULL, NULL, NULL, '101路', '06:30', '21:00', '荣昌北站', '万灵古镇', 1, 'running', '运行中', '#FF7D00', '#FFF2E8', 1, 1, 'expand', 0, '荣昌北站', '昌州大道口', '5分钟', '[{\"label\":\"上行\",\"from\":\"荣昌北站\",\"to\":\"万灵古镇\",\"firstBus\":\"06:30\",\"lastBus\":\"21:00\",\"interval\":\"8-12分钟\"},{\"label\":\"下行\",\"from\":\"万灵古镇\",\"to\":\"荣昌北站\",\"firstBus\":\"06:45\",\"lastBus\":\"21:15\",\"interval\":\"8-12分钟\"}]', '{\"currentStation\":\"荣昌北站\",\"nextStation\":\"昌州大道口\",\"eta\":\"约 5 分钟\",\"speed\":\"28 km/h\",\"vehicleNo\":\"渝C·A8821\"}', '[{\"id\":\"1\",\"label\":\"当前位置\",\"value\":\"荣昌北站（始发）\",\"time\":\"刚刚\"},{\"id\":\"2\",\"label\":\"下一站\",\"value\":\"昌州大道口 · 预计 5 分钟\",\"time\":\"1 分钟前\"},{\"id\":\"3\",\"label\":\"发车间隔\",\"value\":\"8-12 分钟一班\",\"time\":\"实时\"}]', 1, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_route` VALUES (2, '205', NULL, NULL, NULL, '205路', '07:00', '19:30', '区行政中心', '安富街道', 1, 'running', '运行中', '#2B85E4', '#E8F2FF', 0, 0, 'location', 4, '陶艺工坊', '非遗展示中心', '3分钟', '[{\"label\":\"上行\",\"from\":\"区行政中心\",\"to\":\"安富街道\",\"firstBus\":\"07:00\",\"lastBus\":\"19:30\",\"interval\":\"10-15分钟\"},{\"label\":\"下行\",\"from\":\"安富街道\",\"to\":\"区行政中心\",\"firstBus\":\"07:15\",\"lastBus\":\"19:45\",\"interval\":\"10-15分钟\"}]', '{\"currentStation\":\"陶艺工坊\",\"nextStation\":\"非遗展示中心\",\"eta\":\"约 3 分钟\",\"speed\":\"32 km/h\",\"vehicleNo\":\"渝C·B2056\"}', '[{\"id\":\"1\",\"label\":\"当前位置\",\"value\":\"陶艺工坊\",\"time\":\"刚刚\"},{\"id\":\"2\",\"label\":\"下一站\",\"value\":\"非遗展示中心 · 预计 3 分钟\",\"time\":\"30 秒前\"},{\"id\":\"3\",\"label\":\"车辆状态\",\"value\":\"运行中 · 车内较空\",\"time\":\"实时\"}]', 2, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_route` VALUES (3, '303', NULL, NULL, NULL, '303路', '06:00', '22:00', '荣昌火车站', '安陶小镇', 1, 'running', '运行中', '#2EAF6A', '#E8F8EF', 0, 1, 'expand', 3, '安陶博物馆', '陶艺工坊', '4分钟', '[{\"label\":\"上行\",\"from\":\"荣昌火车站\",\"to\":\"安陶小镇\",\"firstBus\":\"06:00\",\"lastBus\":\"22:00\",\"interval\":\"6-10分钟\"},{\"label\":\"下行\",\"from\":\"安陶小镇\",\"to\":\"荣昌火车站\",\"firstBus\":\"06:10\",\"lastBus\":\"22:10\",\"interval\":\"6-10分钟\"}]', '{\"currentStation\":\"安陶博物馆\",\"nextStation\":\"陶艺工坊\",\"eta\":\"约 4 分钟\",\"speed\":\"25 km/h\",\"vehicleNo\":\"渝C·C3033\"}', '[{\"id\":\"1\",\"label\":\"当前位置\",\"value\":\"安陶博物馆\",\"time\":\"刚刚\"},{\"id\":\"2\",\"label\":\"下一站\",\"value\":\"陶艺工坊 · 预计 4 分钟\",\"time\":\"1 分钟前\"},{\"id\":\"3\",\"label\":\"发车间隔\",\"value\":\"6-10 分钟一班\",\"time\":\"实时\"}]', 3, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_route` VALUES (4, 'xian306', 4, NULL, NULL, '游5（306路）', '07:00', '19:00', '西安东站', '兵马俑', 1, 'running', '运行中', '#C62828', '#FFEBEE', 1, 1, 'expand', 0, NULL, NULL, NULL, NULL, NULL, NULL, 10, '2026-09-10 09:44:16', '2026-09-10 09:44:16');

-- ----------------------------
-- Table structure for tb_transport_schedule
-- ----------------------------
DROP TABLE IF EXISTS `tb_transport_schedule`;
CREATE TABLE `tb_transport_schedule`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `route_id` bigint(0) NOT NULL COMMENT '线路ID',
  `station_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '站点名称',
  `times_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '到站时刻 JSON 数组',
  `sort_order` int(0) NOT NULL DEFAULT 0 COMMENT '站点顺序',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_transport_schedule_route`(`route_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '公交时刻表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_transport_schedule
-- ----------------------------
INSERT INTO `tb_transport_schedule` VALUES (1, 1, '荣昌北站', '[\"06:30\",\"06:42\",\"06:54\",\"07:06\",\"07:18\"]', 1, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_schedule` VALUES (2, 1, '昌州大道口', '[\"06:38\",\"06:50\",\"07:02\",\"07:14\",\"07:26\"]', 2, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_schedule` VALUES (3, 1, '人民广场', '[\"06:45\",\"06:57\",\"07:09\",\"07:21\",\"07:33\"]', 3, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_schedule` VALUES (4, 1, '荣昌中学', '[\"06:52\",\"07:04\",\"07:16\",\"07:28\",\"07:40\"]', 4, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_schedule` VALUES (5, 1, '安富路口', '[\"07:00\",\"07:12\",\"07:24\",\"07:36\",\"07:48\"]', 5, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_schedule` VALUES (6, 1, '万灵古镇', '[\"07:25\",\"07:37\",\"07:49\",\"08:01\",\"08:13\"]', 6, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_schedule` VALUES (8, 2, '区行政中心', '[\"07:00\",\"07:15\",\"07:30\",\"07:45\",\"08:00\"]', 1, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_schedule` VALUES (9, 2, '昌元大道', '[\"07:08\",\"07:23\",\"07:38\",\"07:53\",\"08:08\"]', 2, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_schedule` VALUES (10, 2, '荣昌体育馆', '[\"07:16\",\"07:31\",\"07:46\",\"08:01\",\"08:16\"]', 3, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_schedule` VALUES (11, 2, '安陶博物馆', '[\"07:24\",\"07:39\",\"07:54\",\"08:09\",\"08:24\"]', 4, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_schedule` VALUES (12, 2, '安富街道', '[\"07:55\",\"08:10\",\"08:25\",\"08:40\",\"08:55\"]', 5, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_schedule` VALUES (15, 3, '荣昌火车站', '[\"06:00\",\"06:10\",\"06:20\",\"06:30\",\"06:40\"]', 1, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_schedule` VALUES (16, 3, '昌元街道', '[\"06:08\",\"06:18\",\"06:28\",\"06:38\",\"06:48\"]', 2, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_schedule` VALUES (17, 3, '荣昌体育馆', '[\"06:15\",\"06:25\",\"06:35\",\"06:45\",\"06:55\"]', 3, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_schedule` VALUES (18, 3, '安陶博物馆', '[\"06:22\",\"06:32\",\"06:42\",\"06:52\",\"07:02\"]', 4, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_schedule` VALUES (19, 3, '安陶小镇', '[\"06:45\",\"06:55\",\"07:05\",\"07:15\",\"07:25\"]', 5, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_schedule` VALUES (22, 4, '西安东站', '[\"07:00\",\"07:30\",\"08:00\",\"08:30\",\"09:00\"]', 1, '2026-09-10 09:44:16', '2026-09-10 09:44:16');
INSERT INTO `tb_transport_schedule` VALUES (23, 4, '纺织城枢纽站', '[\"07:20\",\"07:50\",\"08:20\",\"08:50\",\"09:20\"]', 2, '2026-09-10 09:44:16', '2026-09-10 09:44:16');
INSERT INTO `tb_transport_schedule` VALUES (24, 4, '华清池', '[\"08:10\",\"08:40\",\"09:10\",\"09:40\",\"10:10\"]', 3, '2026-09-10 09:44:16', '2026-09-10 09:44:16');
INSERT INTO `tb_transport_schedule` VALUES (25, 4, '兵马俑', '[\"08:40\",\"09:10\",\"09:40\",\"10:10\",\"10:40\"]', 4, '2026-09-10 09:44:16', '2026-09-10 09:44:16');

-- ----------------------------
-- Table structure for tb_transport_station
-- ----------------------------
DROP TABLE IF EXISTS `tb_transport_station`;
CREATE TABLE `tb_transport_station`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `route_id` bigint(0) NOT NULL COMMENT '线路ID',
  `station_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '站点名称',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度',
  `sort_order` int(0) NOT NULL DEFAULT 0 COMMENT '站点顺序',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_transport_station_route`(`route_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '公交站点' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_transport_station
-- ----------------------------
INSERT INTO `tb_transport_station` VALUES (1, 1, '荣昌北站', 29.418500, 105.594200, 1, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (2, 1, '昌州大道口', 29.416800, 105.597500, 2, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (3, 1, '人民广场', 29.414200, 105.600800, 3, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (4, 1, '荣昌中学', 29.411500, 105.604100, 4, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (5, 1, '安富路口', 29.408800, 105.607400, 5, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (6, 1, '陶艺博物馆', 29.406100, 105.610700, 6, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (7, 1, '夏布小镇', 29.403400, 105.614000, 7, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (8, 1, '荣昌大桥', 29.400700, 105.617300, 8, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (9, 1, '古昌镇', 29.398000, 105.620600, 9, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (10, 1, '濑溪河桥', 29.395300, 105.623900, 10, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (11, 1, '万灵入口', 29.392600, 105.627200, 11, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (12, 1, '万灵古镇', 29.389900, 105.630500, 12, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (16, 2, '区行政中心', 29.412000, 105.592000, 1, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (17, 2, '昌元大道', 29.410000, 105.595000, 2, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (18, 2, '荣昌体育馆', 29.408000, 105.598000, 3, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (19, 2, '安陶博物馆', 29.406000, 105.601000, 4, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (20, 2, '陶艺工坊', 29.404000, 105.604000, 5, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (21, 2, '非遗展示中心', 29.402000, 105.607000, 6, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (22, 2, '安陶大道', 29.400000, 105.610000, 7, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (23, 2, '安富路口', 29.398000, 105.613000, 8, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (24, 2, '安富镇中心', 29.396000, 105.616000, 9, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (25, 2, '安富街道', 29.394000, 105.619000, 10, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (31, 3, '荣昌火车站', 29.405000, 105.588000, 1, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (32, 3, '昌元街道', 29.407000, 105.591000, 2, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (33, 3, '荣昌体育馆', 29.409000, 105.594000, 3, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (34, 3, '安陶博物馆', 29.411000, 105.597000, 4, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (35, 3, '陶艺工坊', 29.413000, 105.600000, 5, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (36, 3, '非遗展示中心', 29.415000, 105.603000, 6, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (37, 3, '安陶大道', 29.417000, 105.606000, 7, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (38, 3, '安陶小镇', 29.419000, 105.609000, 8, '2026-09-10 09:44:07', '2026-09-10 09:44:07');
INSERT INTO `tb_transport_station` VALUES (46, 4, '西安东站', 34.320000, 109.060000, 1, '2026-09-10 09:44:16', '2026-09-10 09:44:16');
INSERT INTO `tb_transport_station` VALUES (47, 4, '纺织城枢纽站', 34.265000, 109.065000, 2, '2026-09-10 09:44:16', '2026-09-10 09:44:16');
INSERT INTO `tb_transport_station` VALUES (48, 4, '洪庆', 34.210000, 109.100000, 3, '2026-09-10 09:44:16', '2026-09-10 09:44:16');
INSERT INTO `tb_transport_station` VALUES (49, 4, '西安工程大学临潼校区', 34.370000, 109.190000, 4, '2026-09-10 09:44:16', '2026-09-10 09:44:16');
INSERT INTO `tb_transport_station` VALUES (50, 4, '西安科技大学临潼校区', 34.365000, 109.200000, 5, '2026-09-10 09:44:16', '2026-09-10 09:44:16');
INSERT INTO `tb_transport_station` VALUES (51, 4, '华清池', 34.363056, 109.214167, 6, '2026-09-10 09:44:16', '2026-09-10 09:44:16');
INSERT INTO `tb_transport_station` VALUES (52, 4, '东三岔', 34.375000, 109.240000, 7, '2026-09-10 09:44:16', '2026-09-10 09:44:16');
INSERT INTO `tb_transport_station` VALUES (53, 4, '兵马俑', 34.384994, 109.273240, 8, '2026-09-10 09:44:16', '2026-09-10 09:44:16');

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user`  (
  `user_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `user_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '用户名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '头像',
  `sex` int(0) NULL DEFAULT NULL COMMENT '性别 1男 2女',
  `age` int(0) NULL DEFAULT NULL COMMENT '年龄',
  `ma_open_id` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '微信小程序openId',
  `mp_open_id` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '微信公众号openId',
  `points` int(0) NULL DEFAULT 0 COMMENT '用户积分',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '密码',
  `salt` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '加密盐',
  `apple_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '苹果id',
  `sys_phone` int(0) NULL DEFAULT NULL COMMENT '手机类型 1安卓 2ios',
  `status` int(0) NULL DEFAULT 1 COMMENT '状态 1正常 2禁用',
  `platform` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '来源 APP 小程序  公众号',
  `cid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'app推送id',
  `is_send_msg` int(0) NULL DEFAULT NULL COMMENT '是否推送消息',
  `wx_img` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '微信提现图片',
  `user_type` int(0) NULL DEFAULT 0 COMMENT '用户类型 0未认证 1普通用户 2企业用户',
  `current_destination_id` bigint(0) NULL DEFAULT NULL COMMENT '当前目的地ID（tb_destination.id）',
  `is_vip` int(0) NULL DEFAULT 0 COMMENT '是否是会员(0不是 1是)',
  `vip_end_time` datetime(0) NULL DEFAULT NULL COMMENT 'vip到期时间',
  `invitation_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '邀请码',
  `commission_rate` decimal(10, 2) NULL DEFAULT NULL COMMENT '佣金比例',
  `fei_rate` decimal(10, 2) NULL DEFAULT NULL COMMENT '非直属佣金',
  `publish_num` int(0) NULL DEFAULT 0 COMMENT '团游发布次数',
  `photo` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '背景图',
  `signature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '个人签名',
  `inviter_user_id` bigint(0) NULL DEFAULT NULL COMMENT '邀请人用户id',
  `superior_invitation_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '上级邀请码',
  `commission_rate_level1` decimal(8, 4) NULL DEFAULT NULL COMMENT 'level1 rate',
  `commission_rate_level2` decimal(8, 4) NULL DEFAULT NULL COMMENT 'level2 rate',
  `referral_commission_rate` decimal(10, 4) NULL DEFAULT NULL COMMENT '邀请分佣比例，如0.05=5%',
  `referral_commission_rate_level2` decimal(10, 4) NULL DEFAULT NULL COMMENT '二级邀请分佣比例，如0.02=2%',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `deposit` decimal(10, 2) NULL DEFAULT NULL COMMENT '保证金',
  PRIMARY KEY (`user_id`) USING BTREE,
  INDEX `idx_user_current_destination_id`(`current_destination_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user
-- ----------------------------
INSERT INTO `tb_user` VALUES (2, '18892020826', '18892020826', NULL, NULL, NULL, NULL, NULL, 0, '96e79218965eb72c92a549dd5a330112', NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, 1, NULL, 0, NULL, 'IUIPVG', NULL, NULL, 0, NULL, NULL, NULL, 'HUIPVG', NULL, NULL, NULL, NULL, '2026-09-14 14:03:26', NULL, NULL);
INSERT INTO `tb_user` VALUES (3, '18892020827', '18892020827', NULL, NULL, NULL, NULL, NULL, 0, '96e79218965eb72c92a549dd5a330112', NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, 1, NULL, 0, NULL, 'JUIPVG', NULL, NULL, 0, NULL, NULL, NULL, 'IUIPVG', NULL, NULL, NULL, NULL, '2026-09-21 13:46:12', NULL, NULL);
INSERT INTO `tb_user` VALUES (4, '18892020828', '18892020828', NULL, NULL, NULL, NULL, NULL, 0, '96e79218965eb72c92a549dd5a330112', NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, 1, NULL, 0, NULL, 'KUIPVG', NULL, NULL, 0, NULL, NULL, NULL, 'IUIPVG', NULL, NULL, NULL, NULL, '2026-09-22 10:38:51', NULL, NULL);
INSERT INTO `tb_user` VALUES (5, '叁拾', '18892020830', 'https://tu.mianyazuju.com/2026/09/24/eb045e54-d253-45de-a137-4c1d43ce71ba.png', 2, NULL, NULL, NULL, 0, '96e79218965eb72c92a549dd5a330112', NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, 1, NULL, 0, NULL, 'HVIPVG', NULL, NULL, 0, NULL, NULL, NULL, 'mode', NULL, NULL, NULL, NULL, '2026-09-23 15:38:01', NULL, NULL);

-- ----------------------------
-- Table structure for tb_user_address
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_address`;
CREATE TABLE `tb_user_address`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收货人电话',
  `province` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '省',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '市',
  `district` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '区/县',
  `detail_address` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '详细地址',
  `is_default` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否默认：1是 0否',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_address_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户收货地址' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_address
-- ----------------------------
INSERT INTO `tb_user_address` VALUES (1, 4, '十八', '18892020828', '陕西省', '西安市', '新城区', '永兴坊', 1, '2026-09-24 11:38:07', '2026-09-24 11:38:06');
INSERT INTO `tb_user_address` VALUES (2, 2, '十六', '18892020826', '陕西省', '西安市', '新城区', '东一路', 1, '2026-09-24 15:51:41', '2026-09-24 15:51:40');

-- ----------------------------
-- Table structure for tb_user_coupon
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_coupon`;
CREATE TABLE `tb_user_coupon`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `coupon_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户券业务编码',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `template_id` bigint(0) NOT NULL COMMENT '模板ID',
  `template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板业务编码',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '优惠券名称',
  `amount` decimal(12, 2) NOT NULL COMMENT '优惠金额',
  `min_amount` decimal(12, 2) NOT NULL COMMENT '最低消费门槛',
  `scope` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'all' COMMENT '适用范围',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1未使用 2已使用 3已过期',
  `valid_start` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `valid_end` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `used_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '使用的订单号',
  `used_time` datetime(0) NULL DEFAULT NULL COMMENT '使用时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_coupon_code`(`coupon_code`) USING BTREE,
  INDEX `idx_user_coupon_user_id`(`user_id`) USING BTREE,
  INDEX `idx_user_coupon_template_id`(`template_id`) USING BTREE,
  INDEX `idx_user_coupon_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户优惠券' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_coupon
-- ----------------------------
INSERT INTO `tb_user_coupon` VALUES (1, 'CPN-2-1789630060030', 2, 5, 'tpl-all-5', '周末特惠5元券', 5.00, 30.00, 'all', 2, '2026-09-17 15:27:40', '2026-09-24 23:59:59', 'HT1789638031264709', '2026-09-17 17:40:31', '2026-09-17 15:27:40', '2026-09-18 15:09:24');
INSERT INTO `tb_user_coupon` VALUES (2, 'CPN-2-1789717431034', 2, 4, 'tpl-goods-20', '好物满80减20', 20.00, 80.00, 'goods', 1, '2026-09-18 15:43:51', '2026-10-18 23:59:59', NULL, NULL, '2026-09-18 15:43:51', '2026-09-18 15:43:51');
INSERT INTO `tb_user_coupon` VALUES (3, 'CPN-2-1789717740037', 2, 2, 'tpl-hotel-30', '酒店满300减30', 30.00, 300.00, 'hotel', 2, '2026-09-18 15:49:00', '2026-11-17 23:59:59', 'HT1789718796601712', '2026-09-18 16:06:37', '2026-09-18 15:49:00', '2026-09-18 16:06:36');
INSERT INTO `tb_user_coupon` VALUES (4, 'CPN-2-1789867673312', 2, 1, 'tpl-all-10', '新人满100减10', 10.00, 100.00, 'all', 1, '2026-09-20 09:27:53', '2026-10-20 23:59:59', NULL, NULL, '2026-09-20 09:27:53', '2026-09-23 11:23:06');
INSERT INTO `tb_user_coupon` VALUES (5, 'CPN-4-1790226280067', 4, 5, 'tpl-all-5', '周末特惠5元券', 5.00, 30.00, 'all', 1, '2026-09-24 13:04:40', '2026-10-01 23:59:59', NULL, NULL, '2026-09-24 13:04:40', '2026-09-24 13:04:40');
INSERT INTO `tb_user_coupon` VALUES (6, 'CPN-4-1790226280504', 4, 4, 'tpl-goods-20', '好物满80减20', 20.00, 80.00, 'goods', 1, '2026-09-24 13:04:41', '2026-10-25 00:00:00', NULL, NULL, '2026-09-24 13:04:41', '2026-09-24 13:04:40');
INSERT INTO `tb_user_coupon` VALUES (7, 'CPN-4-1790226280948', 4, 3, 'tpl-route-15', '线路满50减15', 15.00, 50.00, 'route', 1, '2026-09-24 13:04:41', '2026-11-09 00:00:00', NULL, NULL, '2026-09-24 13:04:41', '2026-09-24 13:04:40');
INSERT INTO `tb_user_coupon` VALUES (8, 'CPN-4-1790226281418', 4, 2, 'tpl-hotel-30', '酒店满300减30', 30.00, 300.00, 'hotel', 1, '2026-09-24 13:04:41', '2026-11-23 23:59:59', NULL, NULL, '2026-09-24 13:04:41', '2026-09-24 13:04:41');
INSERT INTO `tb_user_coupon` VALUES (9, 'CPN-4-1790226282072', 4, 1, 'tpl-all-10', '新人满100减10', 10.00, 100.00, 'all', 1, '2026-09-24 13:04:42', '2026-10-24 23:59:59', NULL, NULL, '2026-09-24 13:04:42', '2026-09-24 13:04:42');

-- ----------------------------
-- Table structure for tb_user_wallet
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_wallet`;
CREATE TABLE `tb_user_wallet`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `balance` decimal(12, 2) NOT NULL COMMENT '账户余额',
  `total_recharge` decimal(12, 2) NOT NULL COMMENT '累计充值',
  `total_consume` decimal(12, 2) NOT NULL COMMENT '累计消费',
  `total_withdraw` decimal(12, 2) NOT NULL COMMENT '累计提现',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_wallet_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户钱包' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_wallet
-- ----------------------------
INSERT INTO `tb_user_wallet` VALUES (1, 1, 9013.28, 0.00, 0.00, 0.00, '2026-09-11 09:52:10', '2026-09-24 15:28:11');
INSERT INTO `tb_user_wallet` VALUES (2, 2, 92569.40, 0.00, 33293.80, 100.00, '2026-09-14 14:03:26', '2026-09-24 15:52:58');
INSERT INTO `tb_user_wallet` VALUES (3, 3, 55550.00, 0.00, 5.00, 0.00, '2026-09-21 13:46:12', '2026-09-21 14:30:52');
INSERT INTO `tb_user_wallet` VALUES (4, 4, 96867.50, 0.00, 3131.50, 0.00, '2026-09-22 10:38:51', '2026-09-24 15:29:03');
INSERT INTO `tb_user_wallet` VALUES (5, 5, 997999.00, 0.00, 2000.00, 0.00, '2026-09-23 15:38:01', '2026-09-23 15:43:44');

-- ----------------------------
-- Table structure for tb_user_wallet_transaction
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_wallet_transaction`;
CREATE TABLE `tb_user_wallet_transaction`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `tx_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流水号',
  `tx_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型：recharge/consume/income/withdraw',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流水标题',
  `amount` decimal(12, 2) NOT NULL COMMENT '变动金额（正收入负支出）',
  `balance_after` decimal(12, 2) NOT NULL COMMENT '变动后余额',
  `ref_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联业务单号（订单号/充值单号）',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1成功 0处理中 2失败',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wallet_tx_no`(`tx_no`) USING BTREE,
  INDEX `idx_wallet_tx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_wallet_tx_type`(`tx_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户钱包流水' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_wallet_transaction
-- ----------------------------
INSERT INTO `tb_user_wallet_transaction` VALUES (1, 1, '202609111102038276', 'admin_adjust', '管理员调整余额', 9000.00, 9000.00, 'ADM202609111102037258', 1, '2026-09-11 11:02:04');
INSERT INTO `tb_user_wallet_transaction` VALUES (2, 2, '202609141534199778', 'admin_adjust', '管理员增加余额', 888.00, 888.00, 'ADM202609141534195019', 1, '2026-09-14 15:34:19');
INSERT INTO `tb_user_wallet_transaction` VALUES (3, 2, '202609141535538370', 'withdraw', '提现至微信', -100.00, 788.00, 'WD202609141535535888', 2, '2026-09-14 15:35:53');
INSERT INTO `tb_user_wallet_transaction` VALUES (4, 2, '202609141537242066', 'income', '提现驳回退款', 100.00, 888.00, 'WD202609141535535888', 2, '2026-09-14 15:37:24');
INSERT INTO `tb_user_wallet_transaction` VALUES (5, 2, '202609141538078202', 'withdraw', '提现至支付宝', -100.00, 788.00, 'WD202609141538074693', 1, '2026-09-14 15:38:08');
INSERT INTO `tb_user_wallet_transaction` VALUES (6, 2, '202609171804428184', 'consume', '订单支付', -123.00, 665.00, 'HT1789639481728115', 1, '2026-09-17 18:04:43');
INSERT INTO `tb_user_wallet_transaction` VALUES (7, 2, '202609181205344252', 'consume', '订单支付', -128.00, 537.00, 'HT1789704334409472', 1, '2026-09-18 12:05:34');
INSERT INTO `tb_user_wallet_transaction` VALUES (8, 2, '202609181205341455', 'consume', '订单支付', -256.00, 281.00, 'HT1789704334658237', 1, '2026-09-18 12:05:35');
INSERT INTO `tb_user_wallet_transaction` VALUES (9, 1, '202609181205341645', 'income', '一级邀请佣金', 12.80, 9012.80, 'HT1789704334658237', 1, '2026-09-18 12:05:35');
INSERT INTO `tb_user_wallet_transaction` VALUES (10, 2, '202609181205349402', 'consume', '订单支付', -128.00, 153.00, 'HT1789704334832832', 1, '2026-09-18 12:05:35');
INSERT INTO `tb_user_wallet_transaction` VALUES (11, 2, '202609181448265964', 'consume', '订单支付', -123.00, 30.00, 'HT1789714105261153', 1, '2026-09-18 14:48:27');
INSERT INTO `tb_user_wallet_transaction` VALUES (12, 2, '202609181545461109', 'admin_adjust', '管理员增加余额', 8888.00, 8918.00, 'ADM202609181545469657', 1, '2026-09-18 15:45:47');
INSERT INTO `tb_user_wallet_transaction` VALUES (13, 2, '202609181606376295', 'consume', '订单支付', -354.00, 8564.00, 'HT1789718796601712', 1, '2026-09-18 16:06:38');
INSERT INTO `tb_user_wallet_transaction` VALUES (14, 2, '202609200928106450', 'consume', '订单支付', -128.00, 8436.00, 'HT1789867689344245', 1, '2026-09-20 09:28:10');
INSERT INTO `tb_user_wallet_transaction` VALUES (15, 2, '202609201034441854', 'consume', '保证金缴纳', -5000.00, 3436.00, 'MD202609201033395010', 1, '2026-09-20 10:34:45');
INSERT INTO `tb_user_wallet_transaction` VALUES (16, 2, '202609211127296815', 'consume', '保证金缴纳', -1.00, 3435.00, 'MD202609211127282454', 1, '2026-09-21 11:27:30');
INSERT INTO `tb_user_wallet_transaction` VALUES (17, 3, '202609211428201364', 'admin_adjust', '管理员增加余额', 55555.00, 55555.00, 'ADM202609211428205145', 1, '2026-09-21 14:28:20');
INSERT INTO `tb_user_wallet_transaction` VALUES (18, 3, '202609211430526817', 'consume', '保证金缴纳', -5.00, 55550.00, 'MD202609211430512064', 1, '2026-09-21 14:30:52');
INSERT INTO `tb_user_wallet_transaction` VALUES (19, 4, '202609221049296408', 'admin_adjust', '管理员增加余额', 99999.00, 99999.00, 'ADM202609221049292499', 1, '2026-09-22 10:49:30');
INSERT INTO `tb_user_wallet_transaction` VALUES (20, 4, '202609221310047040', 'consume', '保证金缴纳', -3000.00, 96999.00, 'MD202609221310017630', 1, '2026-09-22 13:10:04');
INSERT INTO `tb_user_wallet_transaction` VALUES (21, 2, '202609231047016003', 'admin_adjust', '管理员增加余额', 99999.00, 103434.00, 'ADM202609231047018254', 1, '2026-09-23 10:47:02');
INSERT INTO `tb_user_wallet_transaction` VALUES (22, 2, '202609231102292535', 'consume', '订单支付', -16187.00, 87247.00, 'RT1790131563650207', 1, '2026-09-23 11:02:30');
INSERT INTO `tb_user_wallet_transaction` VALUES (23, 2, '202609231123069107', 'income', '线路订单退款', 16187.00, 103434.00, 'RT1790131563650207', 1, '2026-09-23 11:23:06');
INSERT INTO `tb_user_wallet_transaction` VALUES (24, 2, '202609231124079040', 'consume', '订单支付', -10798.00, 92636.00, 'RT1790133846842435', 1, '2026-09-23 11:24:08');
INSERT INTO `tb_user_wallet_transaction` VALUES (25, 5, '202609231543404784', 'admin_adjust', '管理员增加余额', 999999.00, 999999.00, 'ADM202609231543407934', 1, '2026-09-23 15:43:41');
INSERT INTO `tb_user_wallet_transaction` VALUES (26, 5, '202609231543448413', 'consume', '保证金缴纳', -2000.00, 997999.00, 'MD202609231543439433', 1, '2026-09-23 15:43:44');
INSERT INTO `tb_user_wallet_transaction` VALUES (27, 4, '202609241317336949', 'consume', '订单支付', -23.90, 96975.10, 'OD1790227052165352', 1, '2026-09-24 13:17:34');
INSERT INTO `tb_user_wallet_transaction` VALUES (28, 4, '202609241324088571', 'consume', '订单支付', -33.90, 96941.20, 'OD1790227447374118', 1, '2026-09-24 13:24:08');
INSERT INTO `tb_user_wallet_transaction` VALUES (29, 2, '202609241528114193', 'income', '一级邀请佣金', 1.20, 92637.20, 'OD1790227052165352', 1, '2026-09-24 15:28:11');
INSERT INTO `tb_user_wallet_transaction` VALUES (30, 1, '202609241528115104', 'income', '二级邀请佣金', 0.48, 9013.28, 'OD1790227052165352', 1, '2026-09-24 15:28:11');
INSERT INTO `tb_user_wallet_transaction` VALUES (31, 4, '202609241528262885', 'consume', '订单支付', -33.90, 96907.30, 'OD1790234904661588', 1, '2026-09-24 15:28:27');
INSERT INTO `tb_user_wallet_transaction` VALUES (32, 4, '202609241529032031', 'consume', '订单支付', -39.80, 96867.50, 'OD1790234942539774', 1, '2026-09-24 15:29:04');
INSERT INTO `tb_user_wallet_transaction` VALUES (33, 2, '202609241551457606', 'consume', '订单支付', -33.90, 92603.30, 'OD1790236304667593', 1, '2026-09-24 15:51:46');
INSERT INTO `tb_user_wallet_transaction` VALUES (34, 2, '202609241552584049', 'consume', '订单支付', -33.90, 92569.40, 'OD1790236377798864', 1, '2026-09-24 15:52:59');

-- ----------------------------
-- Table structure for tb_wallet_recharge
-- ----------------------------
DROP TABLE IF EXISTS `tb_wallet_recharge`;
CREATE TABLE `tb_wallet_recharge`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `recharge_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '充值单号（对外支付单号）',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `amount` decimal(12, 2) NOT NULL COMMENT '充值金额',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态：0待支付 1已支付 2已取消',
  `pay_method` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付方式：wx/ali',
  `pay_time` datetime(0) NULL DEFAULT NULL COMMENT '支付时间',
  `trade_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '第三方交易号',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wallet_recharge_no`(`recharge_no`) USING BTREE,
  INDEX `idx_wallet_recharge_user_id`(`user_id`) USING BTREE,
  INDEX `idx_wallet_recharge_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '钱包充值单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_wallet_recharge
-- ----------------------------

-- ----------------------------
-- Table structure for tb_withdraw_record
-- ----------------------------
DROP TABLE IF EXISTS `tb_withdraw_record`;
CREATE TABLE `tb_withdraw_record`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `withdraw_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '提现单号',
  `owner_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主体类型：user/merchant',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户ID（owner_type=user）',
  `merchant_id` bigint(0) NULL DEFAULT NULL COMMENT '商户ID（owner_type=merchant）',
  `amount` decimal(12, 2) NOT NULL COMMENT '提现金额',
  `fee_amount` decimal(12, 2) NOT NULL COMMENT 'fee',
  `actual_amount` decimal(12, 2) NULL DEFAULT NULL COMMENT 'actual payout',
  `method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '提现方式：alipay/wechat',
  `account` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '到账账户（脱敏展示）',
  `account_raw` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '到账账户原文',
  `real_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收款姓名',
  `qr_code_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信收款码URL',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'pending' COMMENT '状态：pending/success/failed',
  `remark` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注/审核说明',
  `audit_time` datetime(0) NULL DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_withdraw_no`(`withdraw_no`) USING BTREE,
  INDEX `idx_withdraw_owner_type`(`owner_type`) USING BTREE,
  INDEX `idx_withdraw_user_id`(`user_id`) USING BTREE,
  INDEX `idx_withdraw_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_withdraw_status`(`status`) USING BTREE,
  INDEX `idx_withdraw_method`(`method`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '提现记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_withdraw_record
-- ----------------------------
INSERT INTO `tb_withdraw_record` VALUES (1, 'WD202609141535535888', 'user', 2, NULL, 100.00, 0.00, 100.00, 'wechat', '微信收款码', NULL, NULL, 'https://tu.mianyazuju.com/2026/09/14/41ab8daa-905c-4104-b9ce-354471588ac4.png', 'failed', '啊实打实的', '2026-09-14 15:37:24', '2026-09-14 15:35:53', '2026-09-15 10:52:06');
INSERT INTO `tb_withdraw_record` VALUES (2, 'WD202609141538074693', 'user', 2, NULL, 100.00, 0.00, 100.00, 'alipay', '188****0826', '18892020826', '十六', NULL, 'success', '已到账', '2026-09-14 15:38:22', '2026-09-14 15:38:08', '2026-09-15 10:52:06');
INSERT INTO `tb_withdraw_record` VALUES (3, 'WD202609211131419171', 'merchant', 2, 5, 500.00, 5.00, 495.00, 'alipay', '188****0825', '18892020825', '十五', NULL, 'pending', '预计 1-3 个工作日到账', NULL, '2026-09-21 11:31:42', '2026-09-21 11:31:41');
INSERT INTO `tb_withdraw_record` VALUES (4, 'WD202609211138379161', 'merchant', 2, 5, 150.00, 1.50, 148.50, 'wechat', '微信收款码', NULL, NULL, 'https://tu.mianyazuju.com/2026/09/21/d1cbb9ae-4d17-423f-ac4e-b36ffe93ba34.webp', 'pending', '预计 1-3 个工作日到账', NULL, '2026-09-21 11:38:37', '2026-09-21 11:38:37');

-- ----------------------------
-- Table structure for verify_code
-- ----------------------------
DROP TABLE IF EXISTS `verify_code`;
CREATE TABLE `verify_code`  (
  `id` bigint(0) NOT NULL COMMENT 'id',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '手机号',
  `code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '验证码',
  `status` tinyint(1) NOT NULL COMMENT '状态 1: 未使用 2: 已使用',
  `expire_time` datetime(0) NULL DEFAULT NULL COMMENT '过期时间',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '验证码' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of verify_code
-- ----------------------------
INSERT INTO `verify_code` VALUES (2098227991746904065, '18892020825', '911732', 2, '2026-09-11 10:01:51', '2026-09-11 09:51:51');
INSERT INTO `verify_code` VALUES (2099378220701483010, '18892020826', '897328', 2, '2026-09-14 14:12:27', '2026-09-14 14:02:27');
INSERT INTO `verify_code` VALUES (2101901065604218882, '18892020827', '469365', 1, '2026-09-21 13:17:20', '2026-09-21 13:07:20');
INSERT INTO `verify_code` VALUES (2101910806585868290, '18892020827', '859854', 2, '2026-09-21 13:56:03', '2026-09-21 13:46:03');
INSERT INTO `verify_code` VALUES (2102226049853706242, '18892020828', '658913', 2, '2026-09-22 10:48:42', '2026-09-22 10:38:42');
INSERT INTO `verify_code` VALUES (2102663689447137281, '18892020830', '207593', 2, '2026-09-23 15:47:44', '2026-09-23 15:37:44');

SET FOREIGN_KEY_CHECKS = 1;
