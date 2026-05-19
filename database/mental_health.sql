-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: mental_health
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `mental_health`
--

/*!40000 DROP DATABASE IF EXISTS `mental_health`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `mental_health` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `mental_health`;

--
-- Table structure for table `appointment`
--

DROP TABLE IF EXISTS `appointment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appointment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `counselor_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `appointment_date` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `time_slot` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'individual',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appointment`
--

LOCK TABLES `appointment` WRITE;
/*!40000 ALTER TABLE `appointment` DISABLE KEYS */;
INSERT INTO `appointment` VALUES (3,14,'张老师','2026-05-20','14:00-15:00','individual','近期压力大，有焦虑情绪','pending','2026-05-18 10:00:00','2026-05-19 20:19:50'),(4,7,'李老师','2026-05-21','10:00-11:00','individual','情绪低落，需要倾诉','confirmed','2026-05-18 11:30:00','2026-05-19 20:19:50'),(5,2,'王老师','2026-05-22','15:00-16:00','individual','人际关系困扰','confirmed','2026-05-18 14:00:00','2026-05-19 21:28:10'),(6,3,'张老师','2026-05-23','09:00-10:00','individual','焦虑失眠问题','confirmed','2026-05-18 15:20:00','2026-05-19 20:19:50'),(7,14,'李老师','2026-05-25','14:00-15:00','individual','考前焦虑','cancelled','2026-05-17 09:00:00','2026-05-19 20:19:50'),(8,7,'王老师','2026-05-26','10:00-11:00','individual','家庭关系问题','pending','2026-05-17 10:00:00','2026-05-19 20:19:50'),(9,1,'张老师','2026-05-27','16:00-17:00','individual','职业规划咨询','confirmed','2026-05-17 08:00:00','2026-05-19 20:19:50'),(10,5,'李老师','2026-05-19','14:00-15:00','individual','情绪管理','cancelled','2026-05-16 09:30:00','2026-05-19 20:19:50'),(11,2,'王老师','2026-05-28','09:00-10:00','group','团体治疗','pending','2026-05-16 11:00:00','2026-05-19 20:19:50'),(12,10,'张老师','2026-05-29','15:00-16:00','individual','学业压力','confirmed','2026-05-16 14:00:00','2026-05-19 20:19:50');
/*!40000 ALTER TABLE `appointment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `article`
--

DROP TABLE IF EXISTS `article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `summary` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` longtext COLLATE utf8mb4_unicode_ci,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cover_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `author` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `view_count` int DEFAULT '0',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'draft',
  `is_recommended` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article`
--

LOCK TABLES `article` WRITE;
/*!40000 ALTER TABLE `article` DISABLE KEYS */;
INSERT INTO `article` VALUES (1,'认识焦虑：你的身体在保护你','焦虑不是敌人，而是身体的一种保护机制。了解焦虑的根源，学会与之共处。','<h3>焦虑的本质</h3><p>焦虑是人体面对潜在威胁时的一种自然反应。当我们感到焦虑时，身体会释放肾上腺素，进入\"战或逃\"状态。这在原始社会帮助我们躲避危险，但在现代社会中，这种反应可能被过度激活。</p><h3>焦虑的症状</h3><ul><li>心悸、心跳加速</li><li>呼吸急促</li><li>肌肉紧张</li><li>注意力难以集中</li><li>睡眠障碍</li></ul><h3>如何管理焦虑</h3><p>1. <strong>深呼吸练习</strong>：当焦虑来袭时，尝试4-7-8呼吸法——吸气4秒，屏息7秒，呼气8秒。</p><p>2. <strong>正念冥想</strong>：每天花10分钟观察自己的呼吸和身体感受，不加评判。</p><p>3. <strong>规律运动</strong>：每周3次30分钟的有氧运动可以显著降低焦虑水平。</p><p>4. <strong>减少咖啡因</strong>：咖啡因会刺激神经系统，加重焦虑症状。</p>','knowledge',NULL,'心理咨询师 王老师',128,'published',1,'2026-04-30 23:17:21','2026-05-10 23:17:21'),(2,'抑郁不是软弱：理解抑郁症','抑郁症是一种真实的疾病，不是性格缺陷。打破污名，从理解开始。','<h3>抑郁症不是\"想开点\"就能好的</h3><p>抑郁症是一种常见的心理健康障碍，影响着全球超过2.6亿人。它不是\"心情不好\"，也不是\"矫情\"，而是一种需要专业治疗的疾病。</p><h3>常见症状</h3><ul><li>持续的情绪低落，几乎每天发生</li><li>对平时喜欢的事物失去兴趣</li><li>食欲显著改变（增加或减少）</li><li>睡眠问题（失眠或嗜睡）</li><li>精力不足，感到疲惫</li><li>自我价值感低</li><li>注意力不集中</li><li>反复出现死亡念头</li></ul><h3>何时寻求帮助</h3><p>如果上述症状持续超过两周，并影响到你的工作、学习或人际关系，请务必寻求专业心理医生的帮助。</p><p>记住：抑郁症是可以治疗的。心理治疗和药物治疗都有很好的效果。</p>','knowledge',NULL,'临床心理师 李医生',256,'published',1,'2026-05-01 23:17:21','2026-05-10 23:17:21'),(3,'正念冥想入门指南','每天10分钟，学会正念冥想，让心灵找到平静的港湾。','<h3>什么是正念冥想</h3><p>正念（Mindfulness）是一种有意识地、不加评判地关注当下的方式。源于佛教禅修，但已被现代心理学广泛研究和应用。</p><h3>基础练习步骤</h3><p><strong>第一步：找安静的地方坐下</strong></p><p>坐在椅子上或垫子上，保持脊柱挺直但不僵硬。</p><p><strong>第二步：关注呼吸</strong></p><p>闭上眼睛，把注意力放在呼吸上。感受空气进入鼻腔、充满肺部、然后缓缓呼出。</p><p><strong>第三步：接纳走神</strong></p><p>你会发现注意力会跑掉，这是正常的。当发现走神时，温柔地把注意力带回到呼吸上，不要责备自己。</p><p><strong>第四步：从5分钟开始</strong></p><p>刚开始每天练习5分钟，逐渐延长到10-15分钟。</p><h3>正念的好处</h3><ul><li>减轻压力和焦虑</li><li>改善注意力</li><li>增强情绪调节能力</li><li>提高自我觉察</li><li>改善睡眠质量</li></ul>','therapy',NULL,'正念导师 张老师',89,'published',1,'2026-05-02 23:17:21','2026-05-10 23:17:21'),(4,'如何建立健康的社交边界','学会说\"不\"，保护自己的情感能量。','<h3>什么是社交边界</h3><p>社交边界是我们在人际关系中设定的\"心理围栏\"，它定义了我们可以接受什么，不能接受什么。健康的边界让我们既能与人亲近，又能保护自己的情感和能量。</p><h3>边界类型</h3><ul><li><strong>物理边界</strong>：个人空间、身体接触的舒适度</li><li><strong>情感边界</strong>：分享个人感受的程度、不承担他人的情绪责任</li><li><strong>时间边界</strong>：如何分配自己的时间，学会拒绝</li><li><strong>数字边界</strong>：工作消息的时间界限、社交媒体使用</li></ul><h3>如何设定边界</h3><p>1. 清楚自己的底线：什么让你感到不舒服？</p><p>2. 直接而礼貌地表达：\"我需要一些独处时间。\"</p><p>3. 保持一致性：一旦设定了边界，就要坚持。</p><p>4. 不要过度解释：说\"不\"不需要长篇大论的理由。</p>','knowledge',NULL,'心理咨询师 陈老师',67,'published',1,'2026-05-03 23:17:21','2026-05-10 23:17:21'),(5,'压力管理：在快节奏中找到平衡','压力不是问题，如何应对压力才是关键。学会与压力共舞。','<h3>压力的双面性</h3><p>适度的压力可以激发潜能，提高效率。但长期过度的压力会对身心健康造成损害。关键不在于消除压力，而在于学会管理和调节。</p><h3>压力的信号</h3><ul><li>身体信号：头痛、肌肉酸痛、消化问题</li><li>情绪信号：易怒、焦虑、情绪低落</li><li>行为信号：食欲改变、社交退缩、工作效率下降</li></ul><h3>有效的压力管理技巧</h3><p><strong>1. 时间管理</strong></p><p>使用番茄工作法：25分钟专注工作，5分钟休息。每4个番茄钟后休息15-30分钟。</p><p><strong>2. 运动释放</strong></p><p>运动是最好的减压方式之一。即使是10分钟的散步也能改善情绪。</p><p><strong>3. 社交支持</strong></p><p>与信任的朋友或家人聊聊你的压力。分享本身就是一种释放。</p><p><strong>4. 兴趣爱好的力量</strong></p><p>培养一个与工作无关的兴趣爱好，让你从压力中暂时抽离。</p>','knowledge',NULL,'心理健康顾问 刘老师',210,'published',1,'2026-05-04 23:17:21','2026-05-10 23:17:21'),(6,'改善人际关系的五个沟通技巧','良好的沟通是健康人际关系的基石。学会这些技巧，让关系更融洽。','<h3>1. 积极倾听</h3><p>真正的倾听不是等待发言的机会，而是努力理解对方的感受和需求。保持眼神接触，点头示意，适时回应。</p><h3>2. \"我\"语言表达</h3><p>用\"我感到...\"代替\"你总是...\"。前者表达感受，后者容易引发防御。</p><p>❌ \"你从来不听我说话。\"</p><p>✅ \"当我说话时没有回应，我感到不被重视。\"</p><h3>3. 非暴力沟通</h3><p>观察 → 感受 → 需要 → 请求。四个步骤清晰表达自己，不指责对方。</p><h3>4. 确认理解</h3><p>\"我听到你说的是……对吗？\" 这避免了误解，也让对方感到被倾听。</p><h3>5. 学会暂停</h3><p>当情绪激烈时，请求暂停：\"我现在有点激动，我们需要休息10分钟再谈。\"</p>','knowledge',NULL,'心理咨询师 王老师',145,'published',1,'2026-05-05 23:17:21','2026-05-10 23:17:21'),(7,'如何帮助有心理健康困扰的朋友','当朋友处于心理困扰时，你的支持可能比想象中更有力量。','<h3>第一步：学会识别信号</h3><p>朋友可能正在经历心理困扰的迹象包括：社交退缩、情绪波动剧烈、对平时喜欢的事物失去兴趣、饮食和睡眠显著变化、频繁提到无助或无价值感。</p><h3>第二步：如何开口</h3><p>找一个安静私密的环境，用关心的语气说：\"我最近注意到你有些变化，我有点担心你。你愿意和我聊聊吗？\"</p><h3>第三步：倾听而非建议</h3><p>大多数人不需要解决方案，他们需要被听见。不要急于给出建议，先理解他们的感受。</p><h3>第四步：鼓励寻求专业帮助</h3><p>\"我很愿意支持你，同时我觉得专业人士可能更能帮到你。我陪你去心理咨询中心好吗？\"</p><h3>第五步：照顾好自己</h3><p>支持他人很重要，但也要注意保护自己的心理健康。如果你的朋友有自杀风险，请务必告诉专业人士或联系心理援助热线。</p>','case',NULL,'心理援助员 赵老师',34,'published',0,'2026-05-06 23:17:21','2026-05-10 23:17:21'),(8,'自我关怀：对自己温柔以待','我们常常对他人比对自己更友善。学会自我关怀，是心理健康的重要一步。','<h3>什么是自我关怀</h3><p>自我关怀（Self-Compassion）是指以对待好朋友的方式来对待自己——在困难时刻给予自己温暖和理解，而不是严厉的批评。</p><h3>自我关怀的三要素</h3><p><strong>1. 自我友善</strong></p><p>对自己说\"这确实很难，没关系\"而不是\"我怎么这么没用\"。</p><p><strong>2. 共通人性</strong></p><p>记住每个人都会经历困难和失败，你并不孤单。</p><p><strong>3. 正念觉察</strong></p><p>以平衡的方式觉察自己的情绪，不压抑也不放大。</p><h3>自我关怀练习</h3><p><strong>给自己写一封信：</strong>以一个好朋友的视角，给自己写一封安慰和鼓励的信。</p><p><strong>自我关怀停顿：</strong>当感到压力时，把手放在心口，深呼吸，对自己说：\"这一刻很难，但我会挺过去的。\"</p><p><strong>感恩日记：</strong>每天睡前写下三件值得感恩的事，无论多么微小。</p>','therapy',NULL,'心理咨询师 林老师',179,'published',1,'2026-05-07 23:17:21','2026-05-10 23:17:21');
/*!40000 ALTER TABLE `article` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `assessment`
--

DROP TABLE IF EXISTS `assessment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assessment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cover_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'draft',
  `question_count` int DEFAULT '0',
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估量表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assessment`
--

LOCK TABLES `assessment` WRITE;
/*!40000 ALTER TABLE `assessment` DISABLE KEYS */;
INSERT INTO `assessment` VALUES (1,'抑郁自评量表（SDS）','SDS 由 Zung 于 1965 年编制，用于衡量抑郁状态的轻重程度及其在治疗中的变化。包含 20 个条目，每个条目按 1-4 级评分。','depression',NULL,'published',20,NULL,'2026-05-10 23:17:21','2026-05-10 23:17:21'),(2,'焦虑自评量表（SAS）','SAS 由 Zung 于 1971 年编制，用于评估焦虑患者的主观感受。包含 20 个条目，每个条目按 1-4 级评分。','anxiety',NULL,'published',20,NULL,'2026-05-10 23:17:21','2026-05-10 23:17:21'),(3,'匹兹堡睡眠质量指数（PSQI）','PSQI 用于评估被试者最近一个月的睡眠质量。包含 19 个自评条目和 5 个他评条目，这里采用自评部分。','sleep',NULL,'published',10,NULL,'2026-05-10 23:17:21','2026-05-10 23:17:21');
/*!40000 ALTER TABLE `assessment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `assessment_question`
--

DROP TABLE IF EXISTS `assessment_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assessment_question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assessment_id` bigint NOT NULL,
  `sort_order` int DEFAULT NULL,
  `question_text` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `option_a` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `option_b` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `option_c` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `option_d` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `score_a` int DEFAULT '0',
  `score_b` int DEFAULT '1',
  `score_c` int DEFAULT '2',
  `score_d` int DEFAULT '3',
  `question_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'radio',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估题目';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assessment_question`
--

LOCK TABLES `assessment_question` WRITE;
/*!40000 ALTER TABLE `assessment_question` DISABLE KEYS */;
INSERT INTO `assessment_question` VALUES (1,1,1,'我觉得闷闷不乐，情绪低沉','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(2,1,2,'我觉得一天之中早晨最好','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',4,3,2,1,'radio'),(3,1,3,'我一阵阵哭出来或觉得想哭','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(4,1,4,'我晚上睡眠不好','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(5,1,5,'我吃的跟平常一样多','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',4,3,2,1,'radio'),(6,1,6,'我与异性密切接触时和以往一样感到愉快','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',4,3,2,1,'radio'),(7,1,7,'我发觉我的体重在下降','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(8,1,8,'我有便秘的苦恼','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(9,1,9,'我心跳比平时快','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(10,1,10,'我无缘无故地感到疲乏','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(11,1,11,'我的头脑跟平常一样清楚','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',4,3,2,1,'radio'),(12,1,12,'我觉得经常做的事情并没有困难','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',4,3,2,1,'radio'),(13,1,13,'我觉得不安而平静不下来','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(14,1,14,'我对将来抱有希望','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',4,3,2,1,'radio'),(15,1,15,'我比平常容易生气激动','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(16,1,16,'我觉得做出决定是容易的','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',4,3,2,1,'radio'),(17,1,17,'我觉得自己是个有用的人，有人需要我','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',4,3,2,1,'radio'),(18,1,18,'我的生活过得很有意思','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',4,3,2,1,'radio'),(19,1,19,'我认为如果我死了别人会生活得好些','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(20,1,20,'我仍旧喜欢自己平时喜欢的东西','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',4,3,2,1,'radio'),(21,2,1,'我觉得比平常容易紧张和着急','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(22,2,2,'我无缘无故地感到害怕','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(23,2,3,'我容易心里烦乱或觉得惊恐','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(24,2,4,'我觉得我可能将要发疯','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(25,2,5,'我觉得一切都很好，也不会发生什么不幸','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',4,3,2,1,'radio'),(26,2,6,'我手脚发抖打颤','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(27,2,7,'我因为头痛、颈痛和背痛而苦恼','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(28,2,8,'我觉得容易衰弱和疲乏','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(29,2,9,'我觉得心平气和，并且容易安静坐着','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',4,3,2,1,'radio'),(30,2,10,'我觉得心跳得很快','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(31,2,11,'我因为一阵阵头晕而苦恼','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(32,2,12,'我有晕倒发作，或觉得要晕倒似的','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(33,2,13,'我吸气呼气都感到很容易','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',4,3,2,1,'radio'),(34,2,14,'我的手脚麻木和刺痛','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(35,2,15,'我因为胃痛和消化不良而苦恼','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(36,2,16,'我常常要小便','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(37,2,17,'我的手脚常常是干燥温暖的','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',4,3,2,1,'radio'),(38,2,18,'我脸红发热','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(39,2,19,'我容易入睡并且一夜睡得很好','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',4,3,2,1,'radio'),(40,2,20,'我做噩梦','没有或很少时间','少部分时间','相当多时间','绝大部分/全部时间',1,2,3,4,'radio'),(41,3,1,'近一个月，你晚上通常几点上床睡觉？','9点前','9-10点','10-11点','11点后',1,2,3,4,'radio'),(42,3,2,'近一个月，你从上床到入睡通常需要多长时间？','≤15分钟','16-30分钟','31-60分钟','≥60分钟',1,2,3,4,'radio'),(43,3,3,'近一个月，你早上通常几点起床？','6点前','6-7点','7-8点','8点后',1,2,3,4,'radio'),(44,3,4,'近一个月，你每晚实际睡眠时间有多少？','≥7小时','6-7小时','5-6小时','<5小时',1,2,3,4,'radio'),(45,3,5,'近一个月，你是否因入睡困难而烦恼？','没有','每周少于1次','每周1-2次','每周≥3次',1,2,3,4,'radio'),(46,3,6,'近一个月，你是否因夜间易醒或早醒而烦恼？','没有','每周少于1次','每周1-2次','每周≥3次',1,2,3,4,'radio'),(47,3,7,'近一个月，你是否因夜间去厕所而影响睡眠？','没有','每周少于1次','每周1-2次','每周≥3次',1,2,3,4,'radio'),(48,3,8,'近一个月，你是否因咳嗽或鼾声高而影响睡眠？','没有','每周少于1次','每周1-2次','每周≥3次',1,2,3,4,'radio'),(49,3,9,'近一个月，你是否因做噩梦而影响睡眠？','没有','每周少于1次','每周1-2次','每周≥3次',1,2,3,4,'radio'),(50,3,10,'近一个月，你自认为睡眠质量如何？','很好','较好','较差','很差',1,2,3,4,'radio');
/*!40000 ALTER TABLE `assessment_question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `assessment_record`
--

DROP TABLE IF EXISTS `assessment_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assessment_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `assessment_id` bigint NOT NULL,
  `total_score` int DEFAULT NULL,
  `level` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `suggestion` text COLLATE utf8mb4_unicode_ci,
  `submit_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assessment_record`
--

LOCK TABLES `assessment_record` WRITE;
/*!40000 ALTER TABLE `assessment_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `assessment_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_message`
--

DROP TABLE IF EXISTS `chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_message` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id` bigint unsigned NOT NULL COMMENT '会话ID',
  `sender_role` enum('USER','ASSISTANT','SYSTEM') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送方角色',
  `content` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_chat_message_session_id` (`session_id`),
  KEY `idx_chat_message_create_time` (`create_time`),
  CONSTRAINT `fk_chat_message_session` FOREIGN KEY (`session_id`) REFERENCES `chat_session` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话消息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_message`
--

LOCK TABLES `chat_message` WRITE;
/*!40000 ALTER TABLE `chat_message` DISABLE KEYS */;
INSERT INTO `chat_message` VALUES (1,1,'USER','最近加班很多，感觉很烦躁，不知道怎么调节。','2026-05-07 23:17:02','2026-05-07 23:17:02'),(2,1,'ASSISTANT','可以先试试每天留出15分钟只做让自己放松的小事，例如深呼吸或散步。你愿意说说最近一次烦躁的具体情境吗？','2026-05-07 23:17:02','2026-05-07 23:17:02'),(3,1,'USER','主要是周末也被消息打断，没法休息。','2026-05-07 23:17:02','2026-05-07 23:17:02'),(4,2,'USER','我连续一周入睡困难。','2026-05-07 23:17:02','2026-05-07 23:17:02'),(5,2,'ASSISTANT','入睡困难很常见。我们可以从作息节律入手：固定起床时间、睡前一小时减少屏幕刺激。需要的话我可以和你一起列一个简单的睡前清单。','2026-05-07 23:17:02','2026-05-07 23:17:02'),(6,3,'USER','I feel anxious these days','2026-05-10 21:21:23','2026-05-10 21:21:23'),(7,3,'ASSISTANT','你的感受值得被重视。想听听我的一些建议吗？','2026-05-10 21:21:23','2026-05-10 21:21:23'),(8,4,'USER','我好伤心 作业写不完看','2026-05-10 21:27:07','2026-05-10 21:27:06'),(9,4,'ASSISTANT','抱抱你，一切都会慢慢好起来的。你现在不是一个人。','2026-05-10 21:27:07','2026-05-10 21:27:06'),(10,5,'USER','hello','2026-05-10 22:46:01','2026-05-10 22:46:00'),(11,5,'ASSISTANT','我理解你现在的心情。有时候说出来就会好很多。','2026-05-10 22:46:01','2026-05-10 22:46:00'),(12,6,'USER','I feel anxious lately, cannot sleep well','2026-05-10 23:02:58','2026-05-10 23:02:57'),(13,6,'ASSISTANT','听到你最近感到焦虑和睡不好，我心里很关心你。这种状态一定让你很疲惫吧？有时候，我们的身体和情绪会像这样发出信号，提醒我们需要停下来看看自己。你愿意和我多说说，是什么事情让你感到焦虑，或者这些感觉是从什么时候开始的吗？我在这里陪着你，慢慢说就好。','2026-05-10 23:03:01','2026-05-10 23:03:00'),(14,6,'USER','I feel like I want to end my life','2026-05-10 23:03:17','2026-05-10 23:03:17'),(15,6,'ASSISTANT','听到你这样说，我心里非常难受。我能感受到你现在一定承受着巨大的痛苦，才会产生这样的想法。你的感受是真实的，也值得被认真对待。请先深呼吸一下，试着感受此刻的呼吸，我就在这里陪着你，不会离开。你愿意和我多说一说，是什么让你感到如此绝望吗？【危机预警：建议拨打心理援助热线 400-161-9995】','2026-05-10 23:03:24','2026-05-10 23:03:23'),(16,6,'USER','I feel like I want to end my life','2026-05-10 23:03:24','2026-05-10 23:03:23'),(17,6,'ASSISTANT','我听到了你内心的痛苦，也感受到你正在经历非常艰难的时刻。请相信，你此刻的情绪是被理解的，你并不孤单。每个人的生命都值得被珍视，包括你的。你愿意和我多说说是什么让你感到如此绝望吗？我会在这里陪着你。\n\n【危机预警：建议拨打心理援助热线 400-161-9995】','2026-05-10 23:03:26','2026-05-10 23:03:26'),(18,7,'USER','你好','2026-05-10 23:05:27','2026-05-10 23:05:26'),(19,7,'ASSISTANT','你好呀，我是心聆。很高兴你愿意和我聊聊。无论你此刻是开心、疲惫，还是心里有些话想说，我都愿意安静地听你说。你最近过得怎么样？','2026-05-10 23:05:28','2026-05-10 23:05:28'),(20,7,'USER','我好困 我好累 可是我自己没写完','2026-05-10 23:05:43','2026-05-10 23:05:42'),(21,7,'ASSISTANT','听到你这样说，我能感受到你真的很疲惫。身体在向你发出信号，需要休息了。有时候，我们越是逼迫自己，反而越难集中精力。不如先停下来，给自己几分钟深呼吸，喝杯温水，让大脑和身体都缓一缓。任务可以稍后再继续，但你的健康和情绪才是最重要的。你愿意和我聊聊是什么让你这么坚持要完成它吗？','2026-05-10 23:05:45','2026-05-10 23:05:45'),(22,7,'USER','我想拿高分','2026-05-10 23:06:01','2026-05-10 23:06:00'),(23,7,'ASSISTANT','听起来你对自己有很高的要求，也付出了很多努力，这真的很棒。不过，追求高分的过程中，有没有感到压力很大或者有点焦虑呢？有时候，我们太专注于结果，反而会忽略了过程中的成长。你最近在准备什么考试或任务吗？可以和我聊聊你的感受，我会一直在这里支持你。','2026-05-10 23:06:03','2026-05-10 23:06:02'),(24,7,'USER','我想去好学校','2026-05-10 23:06:13','2026-05-10 23:06:12'),(25,7,'ASSISTANT','我能感受到你对未来有着很高的期待和追求，想要去好学校说明你对自己有要求，也愿意努力去争取更好的机会，这真的很棒。不过，有时候这种压力可能会让你感到焦虑或不安，对吗？能不能和我聊聊，是什么让你对“好学校”这么在意呢？我愿意听你说说。','2026-05-10 23:06:15','2026-05-10 23:06:14'),(26,8,'USER','我不想学习了','2026-05-10 23:08:15','2026-05-10 23:08:14'),(27,8,'ASSISTANT','我能感受到你现在对学习感到有些疲惫和压力，这种情绪是很正常的。学习路上偶尔会有低谷，重要的是你愿意表达出来，这本身就是一种勇气。  \n也许可以试着给自己一点喘息的时间，比如深呼吸几次，或者短暂地做点喜欢的事。你愿意和我说说，是最近的学习任务特别繁重，还是有什么具体的事情让你感到困扰吗？','2026-05-10 23:08:17','2026-05-10 23:08:16'),(28,8,'USER','我讨厌课设','2026-05-10 23:08:22','2026-05-10 23:08:21'),(29,8,'ASSISTANT','听到你这么说，我能感受到你现在的压力和疲惫。课设确实常常让人感到烦躁，尤其是当任务繁重、时间紧张的时候。这种情绪是很正常的，你并不孤单。可以试着和我聊聊，具体是哪一部分让你觉得特别难受吗？也许说出来会让心里轻松一些。','2026-05-10 23:08:30','2026-05-10 23:08:29'),(30,9,'USER','我好想死','2026-05-10 23:16:43','2026-05-10 23:16:43'),(31,9,'ASSISTANT','听到你这样说，我心里很难过。你愿意把这些感受告诉我，说明你信任我，也说明你内心还存着一丝希望。这种痛苦一定压得你很重，我在这里，不会评判你，只想陪你一起看看，是什么让你这么难受。你愿意和我多说一点吗？【危机预警：建议拨打心理援助热线 400-161-9995】\n\n【危机预警：建议拨打心理援助热线 400-161-9995】','2026-05-10 23:16:45','2026-05-10 23:16:45'),(32,10,'USER','快期末了，我感觉好焦虑，怕挂科','2026-05-06 19:00:00','2026-05-11 00:10:44'),(33,10,'ASSISTANT','听到你为期末考试感到焦虑，我完全理解。期末周确实压力很大，不过你已经意识到了这个问题，这就是很好的第一步。可以跟我说说，是哪一科让你最担心吗？','2026-05-06 19:00:30','2026-05-11 00:10:44'),(34,10,'USER','高数，我高数上期就考得不太好','2026-05-06 19:01:00','2026-05-11 00:10:44'),(35,10,'ASSISTANT','高数确实是一门需要花时间的课程。我建议你可以试试：1. 先把往年真题过一遍，了解题型；2. 找班上高数学得好的同学一起复习；3. 每天固定1-2小时专攻高数，不要临时抱佛脚。你平时有去问老师或助教吗？','2026-05-06 19:02:00','2026-05-11 00:10:44'),(36,10,'USER','还没有，有点不敢问','2026-05-06 19:02:30','2026-05-11 00:10:44'),(37,10,'ASSISTANT','不用害怕问问题哦，老师其实很欢迎学生去请教。你可以先把不会的题目整理好，一次性去问，这样效率更高。记住，求助不是软弱，而是对自己的学习负责。加油，我相信你可以的！','2026-05-06 19:03:00','2026-05-11 00:10:44'),(38,11,'USER','感觉和室友有点合不来，他们喜欢打游戏到很晚，我想早睡','2026-05-08 21:30:00','2026-05-11 00:10:44'),(39,11,'ASSISTANT','宿舍作息不一致确实是个很让人困扰的问题。你有和室友聊过这件事吗？有时候大家只是不知道你的习惯，好好沟通可能会有意想不到的效果。','2026-05-08 21:31:00','2026-05-11 00:10:44'),(40,11,'ASSISTANT','你也可以提议一些折中的办法，比如约定晚上11点后戴耳机、调暗屏幕。如果沟通后还是没有改善，也可以和辅导员反映，申请调换宿舍。你的睡眠和健康是最重要的。','2026-05-08 21:32:00','2026-05-11 00:10:44'),(41,12,'USER','我不想做课设了','2026-05-11 00:19:29','2026-05-11 00:19:28'),(42,12,'ASSISTANT','听起来你现在对课设感到有些疲惫和无力，这很正常，面对繁重的任务时，偶尔有这样的情绪是可以理解的。你愿意和我聊聊具体是哪里让你觉得特别困难吗？也许说出来，心里会轻松一些。我会一直在这里听你倾诉的。','2026-05-11 00:19:31','2026-05-11 00:19:31'),(43,12,'USER','我想睡觉','2026-05-11 00:19:35','2026-05-11 00:19:35'),(44,12,'ASSISTANT','听起来你现在很疲惫，想要好好休息一下了。睡眠是身体和心灵修复的重要时刻，能意识到这一点已经很棒了。如果你愿意，可以试着闭上眼睛，深呼吸几次，感受一下身体的放松。有什么特别的事情让你感到累吗？或者，有没有什么烦恼在脑海里盘旋，让你难以安心入睡？我在这里陪着你。','2026-05-11 00:19:37','2026-05-11 00:19:37'),(45,13,'USER','想死','2026-05-11 00:24:30','2026-05-11 00:24:30'),(46,13,'ASSISTANT','我听到你这么说，心里很担心你。你愿意和我说说，是什么让你感到这么痛苦和绝望吗？我在这里，会耐心听你讲，不会评判你。你并不孤单，我们可以一起慢慢梳理这些情绪。\n\n【危机预警：建议拨打心理援助热线 400-161-9995】\n\n【危机预警：建议拨打心理援助热线 400-161-9995】','2026-05-11 00:24:32','2026-05-11 00:24:31'),(47,13,'USER','一起死吧','2026-05-11 00:24:35','2026-05-11 00:24:35'),(48,13,'ASSISTANT','我听到你这句话，心里很担心你。你现在一定非常痛苦，才会说出这样的话。我在这里陪着你，愿意听你说说发生了什么。你不需要一个人承受这些。可以告诉我，是什么让你感到这么绝望吗？','2026-05-11 00:24:37','2026-05-11 00:24:36'),(49,13,'USER','你回复的慢吗','2026-05-11 00:31:10','2026-05-11 00:31:10'),(50,13,'ASSISTANT','亲爱的，别担心，我会一直在你身边，认真听你说的每一句话。可能我需要一点时间来思考如何更好地回应你，但绝不会让你等太久。你现在感觉怎么样？有什么想和我聊聊的吗？我在这里，随时都愿意倾听你的心声。','2026-05-11 00:31:12','2026-05-11 00:31:11');
/*!40000 ALTER TABLE `chat_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_session`
--

DROP TABLE IF EXISTS `chat_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_session` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `title` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会话标题',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/CLOSED',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_chat_session_user_id` (`user_id`),
  CONSTRAINT `fk_chat_session_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话会话';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_session`
--

LOCK TABLES `chat_session` WRITE;
/*!40000 ALTER TABLE `chat_session` DISABLE KEYS */;
INSERT INTO `chat_session` VALUES (1,2,'情绪疏导 - 工作压力','ACTIVE','2026-05-07 23:17:02','2026-05-07 23:17:02'),(2,3,'睡眠困扰咨询','CLOSED','2026-05-07 23:17:02','2026-05-07 23:17:02'),(3,1,'I feel anxious these...','ACTIVE','2026-05-10 21:21:07','2026-05-10 21:21:23'),(4,7,'我好伤心 作业写不完看','ACTIVE','2026-05-10 21:26:56','2026-05-10 21:27:07'),(5,7,'hello','ACTIVE','2026-05-10 22:45:57','2026-05-10 22:46:01'),(6,1,'I feel anxious latel...','ACTIVE','2026-05-10 23:02:49','2026-05-10 23:03:26'),(7,7,'你好','ACTIVE','2026-05-10 23:05:24','2026-05-10 23:06:15'),(8,7,'我不想学习了','ACTIVE','2026-05-10 23:08:09','2026-05-10 23:08:30'),(9,7,'我好想死','ACTIVE','2026-05-10 23:16:38','2026-05-10 23:16:45'),(10,14,'期末焦虑怎么办','ACTIVE','2026-05-06 19:00:00','2026-05-06 19:05:00'),(11,14,'和室友相处的小烦恼','ACTIVE','2026-05-08 21:30:00','2026-05-08 21:35:00'),(12,14,'我不想做课设了','ACTIVE','2026-05-11 00:19:19','2026-05-11 00:19:38'),(13,14,'想死','ACTIVE','2026-05-11 00:24:24','2026-05-11 00:31:12');
/*!40000 ALTER TABLE `chat_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `crisis_alert`
--

DROP TABLE IF EXISTS `crisis_alert`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `crisis_alert` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `alert_level` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '预警等级：1低 2中 3高 4紧急',
  `handle_status` enum('PENDING','PROCESSING','RESOLVED','CLOSED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '处理状态',
  `emergency_contact` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '紧急联系人姓名',
  `emergency_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '紧急联系人电话',
  `trigger_reason` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '触发原因说明',
  `handled_by` bigint unsigned DEFAULT NULL COMMENT '处理人用户ID（管理员）',
  `handle_remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '处理备注',
  `guardian_notified` tinyint DEFAULT '0' COMMENT '是否已通知监护人 0-否 1-是',
  `guardian_notify_time` datetime DEFAULT NULL COMMENT '通知时间',
  `guardian_notify_method` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知方式: 电话/微信/面谈',
  `guardian_notify_remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_crisis_alert_user_id` (`user_id`),
  KEY `idx_crisis_alert_level` (`alert_level`),
  KEY `idx_crisis_alert_status` (`handle_status`),
  KEY `fk_crisis_alert_handler` (`handled_by`),
  CONSTRAINT `fk_crisis_alert_handler` FOREIGN KEY (`handled_by`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_crisis_alert_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='危机预警';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `crisis_alert`
--

LOCK TABLES `crisis_alert` WRITE;
/*!40000 ALTER TABLE `crisis_alert` DISABLE KEYS */;
INSERT INTO `crisis_alert` VALUES (1,2,3,'PROCESSING','王芳（配偶）','13900001111','用户在日记与对话中出现高风险关键词组合，需人工复核。',1,'test',0,NULL,NULL,NULL,'2026-05-07 23:17:02','2026-05-07 23:17:02'),(2,3,1,'PROCESSING','赵敏（朋友）','13900002222','情感分数短期下降触发低等级预警。',1,'加油',0,NULL,NULL,NULL,'2026-05-07 23:17:02','2026-05-07 23:17:02'),(4,14,4,'PROCESSING','张伟（父亲）','13800138000','AI对话中检测到高危关键词：\"觉得活着好累\"，经AI分析判断为中高风险，已自动触发预警。',NULL,NULL,0,NULL,NULL,NULL,'2026-05-08 22:15:00','2026-05-11 00:10:44'),(6,14,5,'PENDING','张伟（父亲）','13800138000','AI对话检测到高危关键词「想死」：用户表示\"最近压力太大，有时候真的想死\"，AI已自动触发预警。',NULL,NULL,0,NULL,NULL,NULL,'2026-05-19 08:30:00','2026-05-19 20:19:50'),(7,7,5,'PROCESSING','李芳（母亲）','13900139000','日记内容检测到高危关键词「活不下去」：用户写道\"感觉活不下去了，每天都很痛苦\"。',1,'已电话联系家长，建议带学生前往心理中心评估。',0,NULL,NULL,NULL,'2026-05-18 22:15:00','2026-05-19 20:19:50'),(8,2,4,'PENDING','王强（父亲）','13700137000','AI对话检测到高危关键词「结束生命」：用户在AI对话中提及多次\"想结束这一切\"。',NULL,NULL,0,NULL,NULL,NULL,'2026-05-18 19:40:00','2026-05-19 20:19:50'),(9,14,4,'RESOLVED','张伟（父亲）','13800138000','日记内容检测到高危关键词「伤害自己」：用户写道\"忍不住用刀划了自己\"。',1,'已安排紧急心理干预，学生情绪已稳定，转介心理咨询中心长期跟进。',0,NULL,NULL,NULL,'2026-05-17 23:10:00','2026-05-19 20:19:50'),(10,3,3,'PENDING','李明（朋友）','13600136000','AI对话检测到中危关键词「崩溃」：用户表示\"感觉快要崩溃了，什么都没意义\"。',NULL,NULL,0,NULL,NULL,NULL,'2026-05-19 06:50:00','2026-05-19 20:19:50'),(11,7,3,'PROCESSING','李芳（母亲）','13900139000','日记内容检测到中危关键词「绝望」：用户写道\"对未来很绝望，不知道该怎么办\"。',1,'已安排本周五心理咨询。',0,NULL,NULL,NULL,'2026-05-17 14:20:00','2026-05-19 20:19:50'),(12,1,2,'PENDING',NULL,NULL,'AI对话检测到关注级关键词「好累」：用户说\"最近真的好累，感觉撑不住了\"。',NULL,NULL,0,NULL,NULL,NULL,'2026-05-19 07:00:00','2026-05-19 20:19:50'),(13,10,2,'RESOLVED',NULL,NULL,'日记内容检测到关注级关键词「没有意义」：用户写道\"每天重复同样的事，觉得没有意义\"。',1,'辅导员已谈话，学生表示只是一时情绪，暂无危险。',0,NULL,NULL,NULL,'2026-05-16 20:30:00','2026-05-19 20:19:50'),(14,14,5,'PENDING','张伟（父亲）','13800138000','AI对话检测到高危关键词「自杀」：用户直接询问\"自杀的话会不会很痛苦\"，AI已触发危机干预。',NULL,NULL,1,'2026-05-19 15:00:00','phone','called father','2026-05-19 09:05:00','2026-05-19 20:19:50'),(15,3,4,'PENDING',NULL,NULL,'系统自动预警：用户连续3天日记情绪评分低于20分（分别为15、12、8分），触发自动预警机制。',NULL,NULL,0,NULL,NULL,NULL,'2026-05-19 06:00:00','2026-05-19 20:19:50');
/*!40000 ALTER TABLE `crisis_alert` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `crisis_keyword`
--

DROP TABLE IF EXISTS `crisis_keyword`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `crisis_keyword` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `keyword` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关键词',
  `risk_level` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '风险等级: high/mid/low',
  `enabled` tinyint DEFAULT '1' COMMENT '是否启用: 1-启用, 0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `keyword` (`keyword`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='危机关键词配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `crisis_keyword`
--

LOCK TABLES `crisis_keyword` WRITE;
/*!40000 ALTER TABLE `crisis_keyword` DISABLE KEYS */;
INSERT INTO `crisis_keyword` VALUES (1,'自杀','high',1,'2026-05-19 19:41:15'),(2,'想死','high',1,'2026-05-19 19:41:15'),(3,'不想活','high',1,'2026-05-19 19:41:15'),(4,'活不下去','high',1,'2026-05-19 19:41:15'),(5,'结束生命','high',1,'2026-05-19 19:41:15'),(6,'伤害自己','high',1,'2026-05-19 19:41:15'),(7,'自残','high',1,'2026-05-19 19:41:15'),(8,'轻生','high',1,'2026-05-19 19:41:15'),(9,'崩溃','mid',1,'2026-05-19 19:41:15'),(10,'绝望','mid',1,'2026-05-19 19:41:15'),(11,'撑不下去','mid',1,'2026-05-19 19:41:15'),(12,'没有意义','low',1,'2026-05-19 19:41:15'),(13,'活着的意义','low',1,'2026-05-19 19:41:15'),(14,'好累','low',1,'2026-05-19 19:41:15'),(15,'死了算了','high',1,'2026-05-19 19:44:10'),(16,'没有活下去的理由','high',1,'2026-05-19 19:44:10'),(17,'想结束自己','high',1,'2026-05-19 19:44:10'),(18,'活着太痛苦','high',1,'2026-05-19 19:44:10'),(19,'我想消失','high',1,'2026-05-19 19:44:10'),(20,'让我去死','high',1,'2026-05-19 19:44:10'),(21,'活够了','high',1,'2026-05-19 19:44:10'),(22,'不想醒过来','high',1,'2026-05-19 19:44:10'),(23,'做个了结','high',1,'2026-05-19 19:44:10'),(24,'受不了','mid',1,'2026-05-19 19:44:10'),(25,'扛不住','mid',1,'2026-05-19 19:44:10'),(26,'坚持不下去了','mid',1,'2026-05-19 19:44:10'),(27,'太煎熬了','mid',1,'2026-05-19 19:44:10'),(28,'好痛苦','mid',1,'2026-05-19 19:44:10'),(29,'无法承受','mid',1,'2026-05-19 19:44:10'),(30,'快要疯了','mid',1,'2026-05-19 19:44:10'),(31,'快崩溃了','mid',1,'2026-05-19 19:44:10'),(32,'太难了','mid',1,'2026-05-19 19:44:10'),(33,'心累','low',1,'2026-05-19 19:44:10'),(34,'没意思','low',1,'2026-05-19 19:44:10'),(35,'烦死了','low',1,'2026-05-19 19:44:10'),(36,'郁闷','low',1,'2026-05-19 19:44:10'),(37,'不开心','low',1,'2026-05-19 19:44:10'),(38,'压力大','low',1,'2026-05-19 19:44:10'),(39,'难过','low',1,'2026-05-19 19:44:10'),(40,'好烦','low',1,'2026-05-19 19:44:10'),(41,'无聊','low',1,'2026-05-19 19:44:10'),(42,'没劲','low',1,'2026-05-19 19:44:10');
/*!40000 ALTER TABLE `crisis_keyword` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diary`
--

DROP TABLE IF EXISTS `diary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diary` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '日记标题',
  `content` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日记内容',
  `mood_tags` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '心情标签（可多值，逗号分隔）',
  `emotion_score` decimal(5,2) DEFAULT NULL COMMENT '情感分数（如 -1.00 ~ 1.00 或 0~100）',
  `emotion_tags` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '情感标签（可多值，逗号分隔）',
  `images` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图片URL列表(逗号分隔)',
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '单张封面图',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_diary_user_id` (`user_id`),
  KEY `idx_diary_create_time` (`create_time`),
  CONSTRAINT `fk_diary_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日记';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diary`
--

LOCK TABLES `diary` WRITE;
/*!40000 ALTER TABLE `diary` DISABLE KEYS */;
INSERT INTO `diary` VALUES (1,2,NULL,'今天完成了项目阶段性汇报，略有紧张但整体顺利。','平静,期待',72.50,'积极,成就感',NULL,NULL,'2026-05-07 23:17:02','2026-05-07 23:17:02'),(2,2,NULL,'晚上失眠，脑子里一直在想下周的安排。','焦虑,疲惫',38.00,'焦虑,压力',NULL,NULL,'2026-05-07 23:17:02','2026-05-07 23:17:02'),(3,3,NULL,'和朋友散步聊天，心情放松了很多。','放松,愉悦',85.00,'愉悦,社交支持',NULL,NULL,'2026-05-07 23:17:02','2026-05-07 23:17:02'),(4,1,NULL,'Updated content.','calm',NULL,NULL,NULL,NULL,'2026-05-10 20:48:34','2026-05-10 20:48:34'),(5,7,NULL,'哈哈哈哈哈','开心',NULL,NULL,NULL,NULL,'2026-05-10 20:50:42','2026-05-10 20:50:42'),(6,1,NULL,'Test diary entry.','happy',NULL,NULL,NULL,NULL,'2026-05-10 21:02:38','2026-05-10 21:02:38'),(7,1,'Anxious Day','I feel so anxious and worried about everything','焦虑',30.00,NULL,NULL,NULL,'2026-05-10 21:55:44','2026-05-10 21:55:44'),(8,7,NULL,'我恨这个世界','愤怒',50.00,NULL,NULL,NULL,'2026-05-10 23:08:02','2026-05-10 23:08:02'),(9,7,NULL,'想死','平静',50.00,NULL,NULL,'/uploads/9ba28976-67dd-4531-b85a-98ee2e9e7be3.jpg','2026-05-10 23:09:44','2026-05-10 23:09:44'),(10,1,'audit','audit check','一般',50.00,NULL,NULL,NULL,'2026-05-10 23:12:45','2026-05-10 23:12:45'),(11,14,'新学期的第一次班会','今天班级开了新学期第一次班会，辅导员讲了这学期的安排。感觉时间过得好快，转眼都大二下学期了。有点焦虑这学期的课程难度，希望自己能跟得上。','焦虑',35.00,NULL,NULL,'/uploads/9ba28976-67dd-4531-b85a-98ee2e9e7be3.jpg','2026-05-04 20:30:00','2026-05-11 00:10:44'),(12,14,'和室友一起打篮球','下午没课，和室友去操场打了两个小时篮球，出了一身汗，感觉整个人都轻松了很多。晚上一起去食堂吃了麻辣香锅，开心！','开心',82.00,NULL,NULL,'/uploads/9ba28976-67dd-4531-b85a-98ee2e9e7be3.jpg','2026-05-05 18:15:00','2026-05-11 00:10:44'),(13,14,'高数作业好难','在图书馆泡了一下午做高数作业，题目好难，卡在一道积分题上好久。周围的同学好像都会做，有点挫败感。不过最后还是解出来了，松了口气。','焦虑',50.00,NULL,NULL,'/uploads/0d5d3639-9db0-4f8b-9ebf-e9195f53aa82.jpg','2026-05-05 21:00:00','2026-05-11 00:10:44'),(14,14,'和爸妈视频通话','晚上和爸妈视频了一个小时，聊了聊最近的生活。妈妈说给我寄了家乡的腊肠，好期待。虽然在学校有时候会觉得孤独，但听到家人的声音就很安心。','平静',50.00,NULL,NULL,'/uploads/7a8b9a64-b401-4f38-a622-f1f26bba872d.png','2026-05-06 20:00:00','2026-05-11 00:10:44'),(15,14,'社团活动圆满成功','作为社团的副部长，今天负责组织了春季招新活动，来了好多新生报名。虽然前前后后忙了整整一周，但看到活动顺利进行，真的很有成就感！','开心',50.00,NULL,NULL,'/uploads/b23fae93-5db3-4225-a8ac-e3f2954a60e7.png','2026-05-07 17:30:00','2026-05-11 00:10:44'),(16,14,'突然想家了','晚上回到宿舍，看到舍友都在各忙各的，突然觉得好想家。翻手机里和家人的合照，眼泪不争气地流下来了。什么时候才能变得更坚强一点呢。','难过',22.00,NULL,NULL,'/uploads/9ba28976-67dd-4531-b85a-98ee2e9e7be3.jpg','2026-05-08 22:10:00','2026-05-11 00:10:44'),(17,14,'期末复习计划','今天制定了期末复习计划表，把剩下一个月的复习时间安排得满满当当的。虽然有点紧张怕时间不够，但有计划总比没有好！加油加油！','平静',60.00,NULL,NULL,'/uploads/9ba28976-67dd-4531-b85a-98ee2e9e7be3.jpg','2026-05-09 16:00:00','2026-05-11 00:10:44'),(18,14,'周末在图书馆自习','周末图书馆人好多，八点来就没位置了，好不容易在三楼角落找到个座。复习了英语和专业课，效率还不错。中午去食堂吃了最爱吃的酸菜鱼，满足~','平静',50.00,NULL,NULL,'/uploads/9ba28976-67dd-4531-b85a-98ee2e9e7be3.jpg','2026-05-10 11:30:00','2026-05-11 00:10:44');
/*!40000 ALTER TABLE `diary` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `emergency_contact`
--

DROP TABLE IF EXISTS `emergency_contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `emergency_contact` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系人姓名',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `relation` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关系',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认 0-否 1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='紧急联系人表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `emergency_contact`
--

LOCK TABLES `emergency_contact` WRITE;
/*!40000 ALTER TABLE `emergency_contact` DISABLE KEYS */;
INSERT INTO `emergency_contact` VALUES (1,1,'Zhang San','13800138000','family',1,'2026-05-10 21:47:54','2026-05-10 21:47:54'),(2,1,'Li Si','13900139000','friend',0,'2026-05-10 21:47:54','2026-05-10 21:47:54'),(3,7,'岩岩','11111111111','朋友',0,'2026-05-10 22:05:57','2026-05-10 22:05:57'),(4,14,'张伟（父亲）','13800138000','父亲',1,'2026-04-10 10:00:00','2026-05-11 00:10:44'),(5,14,'李芳（母亲）','13900139000','母亲',0,'2026-04-10 10:01:00','2026-05-11 00:10:44'),(6,14,'王老师（辅导员）','13600136000','辅导员',0,'2026-04-10 10:02:00','2026-05-11 00:10:44');
/*!40000 ALTER TABLE `emergency_contact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `emotion_record`
--

DROP TABLE IF EXISTS `emotion_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `emotion_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `diary_id` bigint unsigned DEFAULT NULL COMMENT '关联日记ID（可选）',
  `emotion_score` decimal(5,2) NOT NULL COMMENT '情感分数',
  `emotion_label` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '情感标签',
  `source_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'MANUAL' COMMENT '来源：DIARY日记/MANUAL手动/CHAT对话等',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_emotion_record_user_id` (`user_id`),
  KEY `idx_emotion_record_diary_id` (`diary_id`),
  KEY `idx_emotion_record_create_time` (`create_time`),
  CONSTRAINT `fk_emotion_record_diary` FOREIGN KEY (`diary_id`) REFERENCES `diary` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_emotion_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情感记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `emotion_record`
--

LOCK TABLES `emotion_record` WRITE;
/*!40000 ALTER TABLE `emotion_record` DISABLE KEYS */;
INSERT INTO `emotion_record` VALUES (1,2,1,72.50,'积极','DIARY','来源于日记#1','2026-05-07 23:17:02','2026-05-07 23:17:02'),(2,2,2,38.00,'焦虑','DIARY','来源于日记#2','2026-05-07 23:17:02','2026-05-07 23:17:02'),(3,3,NULL,60.00,'平稳','MANUAL','用户自评','2026-05-07 23:17:02','2026-05-07 23:17:02'),(4,3,3,85.00,'愉悦','DIARY','来源于日记#3','2026-05-07 23:17:02','2026-05-07 23:17:02');
/*!40000 ALTER TABLE `emotion_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `emotion_report`
--

DROP TABLE IF EXISTS `emotion_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `emotion_report` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `report_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'WEEKLY' COMMENT '报告类型：WEEKLY/MONTHLY/CUSTOM',
  `period_start` date NOT NULL COMMENT '统计周期开始',
  `period_end` date NOT NULL COMMENT '统计周期结束',
  `summary` text COLLATE utf8mb4_unicode_ci COMMENT '文字摘要',
  `statistics_json` json DEFAULT NULL COMMENT '统计数据JSON（均值、波动等）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_emotion_report_user_id` (`user_id`),
  KEY `idx_emotion_report_period` (`period_start`,`period_end`),
  CONSTRAINT `fk_emotion_report_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情感报告';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `emotion_report`
--

LOCK TABLES `emotion_report` WRITE;
/*!40000 ALTER TABLE `emotion_report` DISABLE KEYS */;
INSERT INTO `emotion_report` VALUES (1,2,'WEEKLY','2026-04-28','2026-05-04','本周情绪波动较大，建议保持规律作息并适度放松。','{\"avg_score\": 55.25, \"max_score\": 72.50, \"min_score\": 38.00, \"record_count\": 2}','2026-05-07 23:17:02','2026-05-07 23:17:02'),(2,3,'WEEKLY','2026-04-28','2026-05-04','本周整体情绪偏积极，社交活动对情绪有正向作用。','{\"avg_score\": 72.50, \"max_score\": 85.00, \"min_score\": 60.00, \"record_count\": 2}','2026-05-07 23:17:02','2026-05-07 23:17:02'),(3,1,'WEEKLY','2026-05-03','2026-05-10','本周共记录了 3 篇日记，平均情绪分为 30 分，最常见的情绪是「calm」。情绪偏低落，建议多与朋友家人沟通，必要时寻求专业帮助。 建议保持规律作息，适当运动，关注自己的心理状态。','{\"topMood\": \"calm\", \"avgScore\": 30, \"diaryCount\": 3}','2026-05-10 22:02:33','2026-05-10 22:02:33'),(4,7,'WEEKLY','2026-05-03','2026-05-10','本周共记录了 1 篇日记，平均情绪分为 0 分，最常见的情绪是「开心」。情绪偏低落，建议多与朋友家人沟通，必要时寻求专业帮助。 建议保持规律作息，适当运动，关注自己的心理状态。','{\"topMood\": \"开心\", \"avgScore\": 0, \"diaryCount\": 1}','2026-05-10 22:46:05','2026-05-10 22:46:05'),(5,7,'WEEKLY','2026-05-03','2026-05-10','本周共记录了 2 篇日记，平均情绪分为 50 分，最常见的情绪是「开心」。情绪状态总体平稳，偶有波动属于正常现象。 建议保持规律作息，适当运动，关注自己的心理状态。','{\"topMood\": \"开心\", \"avgScore\": 50, \"diaryCount\": 2}','2026-05-10 23:08:38','2026-05-10 23:08:38'),(6,1,'WEEKLY','2026-05-03','2026-05-10','本周共记录了 4 篇日记，平均情绪分为 40 分，最常见的情绪是「calm」。情绪偏低落，建议多与朋友家人沟通，必要时寻求专业帮助。 建议保持规律作息，适当运动，关注自己的心理状态。','{\"topMood\": \"calm\", \"avgScore\": 40, \"diaryCount\": 4}','2026-05-10 23:12:45','2026-05-10 23:12:45'),(7,1,'WEEKLY','2026-05-03','2026-05-10','本周共记录了 4 篇日记，平均情绪分为 40 分，最常见的情绪是「calm」。情绪偏低落，建议多与朋友家人沟通，必要时寻求专业帮助。 建议保持规律作息，适当运动，关注自己的心理状态。','{\"trend\": {\"dates\": [\"05-10\"], \"scores\": [40.0]}, \"topMood\": \"calm\", \"avgScore\": 40, \"diaryCount\": 4}','2026-05-10 23:44:27','2026-05-10 23:44:27'),(8,1,'WEEKLY','2026-05-03','2026-05-10','本周共记录了 4 篇日记，平均情绪分为 40 分，最常见的情绪是「calm」。情绪偏低落，建议多与朋友家人沟通，必要时寻求专业帮助。 建议保持规律作息，适当运动，关注自己的心理状态。','{\"trend\": {\"dates\": [\"05-10\"], \"scores\": [40.0]}, \"topMood\": \"calm\", \"avgScore\": 40, \"diaryCount\": 4}','2026-05-10 23:45:45','2026-05-10 23:45:45'),(9,7,'WEEKLY','2026-05-03','2026-05-10','本周共记录了 3 篇日记，平均情绪分为 50 分，最常见的情绪是「开心」。情绪状态总体平稳，偶有波动属于正常现象。 建议保持规律作息，适当运动，关注自己的心理状态。','{\"trend\": {\"dates\": [\"05-10\"], \"scores\": [50.0]}, \"topMood\": \"开心\", \"avgScore\": 50, \"diaryCount\": 3}','2026-05-10 23:46:28','2026-05-10 23:46:28'),(10,7,'WEEKLY','2026-05-03','2026-05-10','本周共记录了 3 篇日记，平均情绪分为 50 分，最常见的情绪是「开心」。情绪状态总体平稳，偶有波动属于正常现象。 建议保持规律作息，适当运动，关注自己的心理状态。','{\"trend\": {\"dates\": [\"05-10\"], \"scores\": [50.0]}, \"topMood\": \"开心\", \"avgScore\": 50, \"diaryCount\": 3}','2026-05-10 23:46:31','2026-05-10 23:46:31'),(11,7,'WEEKLY','2026-05-03','2026-05-10','本周共记录了 3 篇日记，平均情绪分为 50 分，最常见的情绪是「开心」。情绪状态总体平稳，偶有波动属于正常现象。 建议保持规律作息，适当运动，关注自己的心理状态。','{\"trend\": {\"dates\": [\"05-10\"], \"scores\": [50.0]}, \"topMood\": \"开心\", \"avgScore\": 50, \"diaryCount\": 3}','2026-05-10 23:46:42','2026-05-10 23:46:42'),(12,14,'WEEKLY','2026-05-04','2026-05-10','本周共记录了 8 篇日记，平均情绪分为 58 分，最常见的情绪是「平静」和「开心」。整体情绪状态良好，但有少量的焦虑和难过情绪，主要与学业压力和想家有关。建议保持运动习惯和与家人朋友的联系，这对情绪调节很有帮助。','{\"trend\": {\"dates\": [\"05-04\", \"05-05\", \"05-06\", \"05-07\", \"05-08\", \"05-09\", \"05-10\"], \"scores\": [35, 61, 70, 88, 22, 60, 65]}, \"topMood\": \"平静\", \"avgScore\": 58, \"diaryCount\": 8}','2026-05-10 22:00:00','2026-05-11 00:10:44'),(13,14,'WEEKLY','2026-05-04','2026-05-11','本周共记录了 8 篇日记，平均情绪分为 50 分，最常见的情绪是「平静」。情绪状态总体平稳，偶有波动属于正常现象。情绪较为稳定。 建议保持规律作息，适当运动，关注自己的心理状态。','{\"trend\": {\"dates\": [\"05-04\", \"05-05\", \"05-06\", \"05-07\", \"05-08\", \"05-09\", \"05-10\"], \"scores\": [35.0, 66.0, 50.0, 50.0, 22.0, 60.0, 50.0]}, \"topMood\": \"平静\", \"avgScore\": 50, \"diaryCount\": 8}','2026-05-11 00:14:25','2026-05-11 00:14:25'),(14,14,'WEEKLY','2026-05-04','2026-05-11','本周共记录了 8 篇日记，平均情绪分为 50 分，最常见的情绪是「平静」。情绪状态总体平稳，偶有波动属于正常现象。情绪较为稳定。 建议保持规律作息，适当运动，关注自己的心理状态。','{\"trend\": {\"dates\": [\"05-04\", \"05-05\", \"05-06\", \"05-07\", \"05-08\", \"05-09\", \"05-10\"], \"scores\": [35.0, 66.0, 50.0, 50.0, 22.0, 60.0, 50.0]}, \"topMood\": \"平静\", \"avgScore\": 50, \"diaryCount\": 8}','2026-05-11 00:14:30','2026-05-11 00:14:30');
/*!40000 ALTER TABLE `emotion_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mood_record`
--

DROP TABLE IF EXISTS `mood_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mood_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `mood_score` int DEFAULT NULL,
  `mood_tag` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `note` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `record_date` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情绪记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mood_record`
--

LOCK TABLES `mood_record` WRITE;
/*!40000 ALTER TABLE `mood_record` DISABLE KEYS */;
INSERT INTO `mood_record` VALUES (1,1,8,'happy','蹇冩儏涓嶉敊','2026-05-09','2026-05-10 21:08:34'),(2,1,7,'happy','鎸哄ソ鐨','2026-05-08','2026-05-10 21:08:34'),(3,1,5,'calm','涓?埇鑸','2026-05-07','2026-05-10 21:08:34'),(4,1,6,'calm','杩樿?','2026-05-06','2026-05-10 21:08:34'),(5,1,4,'anxious','鏈夌偣鐒﹁檻','2026-05-05','2026-05-10 21:08:34'),(6,1,3,'sad','涓嶅紑蹇','2026-05-04','2026-05-10 21:08:34'),(7,1,6,'happy','鎭㈠?浜','2026-05-03','2026-05-10 21:08:34'),(8,1,7,'happy','寰堝ソ','2026-05-02','2026-05-10 21:08:34'),(9,1,8,'happy','闈炲父寮?績','2026-05-01','2026-05-10 21:08:34'),(10,1,5,'tired','鏈夌偣绱','2026-04-30','2026-05-10 21:08:34'),(11,1,4,'tired','鐤叉儷','2026-04-29','2026-05-10 21:08:34'),(12,1,6,'calm','骞抽潤鐨勪竴澶','2026-04-28','2026-05-10 21:08:34'),(13,1,7,'happy','鎰夊揩','2026-04-27','2026-05-10 21:08:34'),(14,1,5,'calm','鏅??','2026-04-26','2026-05-10 21:08:34'),(15,1,4,'anxious','鏈夌偣绱у紶','2026-04-25','2026-05-10 21:08:34'),(16,1,6,'calm','鏀炬澗','2026-04-24','2026-05-10 21:08:34'),(17,1,8,'happy','澶??浜','2026-04-23','2026-05-10 21:08:34'),(18,1,7,'happy','寮?績','2026-04-22','2026-05-10 21:08:34'),(19,1,3,'sad','闅捐繃','2026-04-21','2026-05-10 21:08:34'),(20,1,5,'calm','骞抽潤','2026-04-20','2026-05-10 21:08:34'),(21,1,6,'happy','涓嶉敊','2026-04-19','2026-05-10 21:08:34'),(22,1,7,'happy','寰堝ソ','2026-04-18','2026-05-10 21:08:34'),(23,1,4,'anxious','鐒﹁檻','2026-04-17','2026-05-10 21:08:34'),(24,1,5,'calm','杩樿?','2026-04-16','2026-05-10 21:08:34'),(25,1,6,'calm','骞崇ǔ','2026-04-15','2026-05-10 21:08:34'),(26,1,7,'happy','鎰夊揩','2026-04-14','2026-05-10 21:08:34'),(27,1,8,'happy','瀹岀編','2026-04-13','2026-05-10 21:08:34'),(28,1,6,'happy','濂','2026-04-12','2026-05-10 21:08:34'),(29,1,5,'tired','绱','2026-04-11','2026-05-10 21:08:34'),(30,1,7,'happy','鍛ㄦ湯鎰夊揩','2026-04-10','2026-05-10 21:08:34'),(31,14,3,'anxious','新学期焦虑，担心课程难度','2026-05-04','2026-05-04 20:35:00'),(32,14,8,'happy','打球运动很开心','2026-05-05','2026-05-05 18:20:00'),(33,14,4,'anxious','高数题目太难了','2026-05-05','2026-05-05 21:05:00'),(34,14,7,'calm','和爸妈视频后很安心','2026-05-06','2026-05-06 20:05:00'),(35,14,9,'happy','社团活动成功举办，超级有成就感','2026-05-07','2026-05-07 17:35:00'),(36,14,2,'sad','想家了，有点难过','2026-05-08','2026-05-08 22:15:00'),(37,14,6,'calm','制定了复习计划，心里有底了','2026-05-09','2026-05-09 16:05:00'),(38,14,6,'calm','图书馆自习效率不错','2026-05-10','2026-05-10 11:35:00'),(39,14,6,'calm','哈哈哈','2026-05-11','2026-05-11 00:12:31');
/*!40000 ALTER TABLE `mood_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_config`
--

DROP TABLE IF EXISTS `system_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_key` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` mediumtext COLLATE utf8mb4_unicode_ci COMMENT '配置值',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_system_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_config`
--

LOCK TABLES `system_config` WRITE;
/*!40000 ALTER TABLE `system_config` DISABLE KEYS */;
INSERT INTO `system_config` VALUES (1,'crisis.keyword.list','自杀,自残,不想活了','危机检测关键词列表（逗号分隔）','2026-05-07 23:17:02','2026-05-07 23:17:02'),(2,'emotion.score.threshold.low','40','情感分数低于该值触发关注提醒','2026-05-07 23:17:02','2026-05-07 23:17:02'),(3,'chat.session.max_per_day','20','普通用户每日新建会话上限','2026-05-07 23:17:02','2026-05-07 23:17:02');
/*!40000 ALTER TABLE `system_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录名',
  `password` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码哈希',
  `nickname` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'user',
  `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint DEFAULT '0' COMMENT '性别',
  `age` int DEFAULT NULL COMMENT '年龄',
  `occupation` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '职业',
  `bio` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '个人简介',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0禁用 1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  KEY `idx_user_role` (`role`),
  KEY `idx_user_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9','系统管理员','13800000001','admin@example.com','admin',NULL,0,NULL,'Software Engineer',NULL,0,1,'2026-05-07 23:17:02','2026-05-10 23:43:58'),(2,'zhangsan','123456','张三','13800000002','zhangsan@example.com','user',NULL,0,NULL,NULL,NULL,0,1,'2026-05-07 23:17:02','2026-05-08 23:28:39'),(3,'lisi','123456','李四','13800000003','lisi@example.com','user',NULL,0,NULL,NULL,NULL,0,1,'2026-05-07 23:17:02','2026-05-08 23:28:39'),(5,'testuser','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','test',NULL,NULL,'user','https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',0,NULL,NULL,NULL,0,1,'2026-05-08 23:35:53','2026-05-08 23:35:53'),(6,'李雨桐','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','李',NULL,NULL,'user','https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',0,NULL,NULL,NULL,0,1,'2026-05-08 23:38:00','2026-05-08 23:38:00'),(7,'liyutong','e0b38b3c13e68f6d91710e80a8d59c17a62bf84ef5ce6477e7931b79c64a1a17','li ',NULL,NULL,'user','https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',0,NULL,NULL,NULL,0,1,'2026-05-10 20:34:03','2026-05-10 20:34:03'),(8,'checktest','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','checktest',NULL,NULL,'user','https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',0,NULL,NULL,NULL,0,1,'2026-05-10 21:30:19','2026-05-10 21:30:19'),(9,'chk_$(date +%s)','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','Updated',NULL,NULL,'user','https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',0,25,'engineer',NULL,1,1,'2026-05-10 21:30:40','2026-05-10 22:15:18'),(10,'newuser','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','New',NULL,NULL,'user',NULL,1,NULL,NULL,NULL,0,1,'2026-05-10 22:15:17','2026-05-10 22:15:17'),(11,'fin_check_1778424245','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','fin_check_1778424245',NULL,NULL,'user','https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',0,NULL,NULL,NULL,0,1,'2026-05-10 22:44:05','2026-05-10 22:44:05'),(12,'audit_1778425961','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','audit_1778425961',NULL,NULL,'user','https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',0,NULL,NULL,NULL,0,1,'2026-05-10 23:12:42','2026-05-10 23:12:42'),(14,'student','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','小明同学','','','user',NULL,1,20,'大学生','开开心心每一天',0,1,'2026-04-01 08:00:00','2026-05-11 00:10:44'),(15,'wangwei','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','王伟','13800001001','wangwei@test.com','user',NULL,1,22,'计算机专业学生',NULL,0,1,'2026-04-01 08:00:00','2026-05-19 20:51:21'),(16,'zhangmei','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','张美','13800001002','zhangmei@test.com','user',NULL,2,21,'英语专业学生',NULL,0,1,'2026-04-02 09:00:00','2026-05-19 20:51:21'),(17,'liujun','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','刘军','13800001003','liujun@test.com','user',NULL,1,23,'机械工程学生',NULL,0,1,'2026-04-03 10:30:00','2026-05-19 20:51:21'),(18,'chenli','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','陈丽','13800001004','chenli@test.com','user',NULL,2,20,'心理学专业学生',NULL,0,1,'2026-04-04 11:00:00','2026-05-19 20:51:21'),(19,'yanghao','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','杨浩','13800001005','yanghao@test.com','user',NULL,1,24,'土木工程学生',NULL,0,1,'2026-04-05 08:30:00','2026-05-19 20:51:21'),(20,'zhaoting','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','赵婷','13800001006','zhaoting@test.com','user',NULL,2,19,'新闻传播学生',NULL,0,1,'2026-04-06 14:00:00','2026-05-19 20:51:21'),(21,'huanglei','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','黄磊','13800001007','huanglei@test.com','user',NULL,1,22,'物理学专业学生',NULL,0,1,'2026-04-07 09:00:00','2026-05-19 20:51:21'),(22,'zhoujie','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','周洁','13800001008','zhoujie@test.com','user',NULL,2,21,'汉语言文学学生',NULL,0,1,'2026-04-08 10:00:00','2026-05-19 20:51:21'),(23,'xuyong','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','徐勇','13800001009','xuyong@test.com','user',NULL,1,23,'体育教育学生',NULL,0,1,'2026-04-09 11:30:00','2026-05-19 20:51:21'),(24,'sunna','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','孙娜','13800001010','sunna@test.com','user',NULL,2,20,'艺术设计学生',NULL,0,1,'2026-04-10 08:00:00','2026-05-19 20:51:21'),(25,'linwei','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','林伟','13800001011','linwei@test.com','user',NULL,1,22,'数学与应用数学学生',NULL,0,1,'2026-04-11 09:00:00','2026-05-19 20:51:21'),(26,'caixue','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','蔡雪','13800001012','caixue@test.com','user',NULL,2,21,'法学专业学生',NULL,0,1,'2026-04-12 10:00:00','2026-05-19 20:51:21'),(27,'shengbiao','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','盛彪','13800001013','shengbiao@test.com','user',NULL,1,24,'化学工程学生',NULL,0,1,'2026-04-13 11:00:00','2026-05-19 20:51:21'),(28,'yaolan','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','姚兰','13800001014','yaolan@test.com','user',NULL,2,19,'音乐学学生',NULL,0,1,'2026-04-14 14:00:00','2026-05-19 20:51:21'),(29,'dengchao','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','邓超','13800001015','dengchao@test.com','user',NULL,1,23,'自动化专业学生',NULL,0,1,'2026-04-15 08:30:00','2026-05-19 20:51:21'),(30,'fanbing','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','范冰','13800001016','fanbing@test.com','user',NULL,2,20,'金融学学生',NULL,0,1,'2026-04-16 09:00:00','2026-05-19 20:51:21'),(31,'tangqiang','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','唐强','13800001017','tangqiang@test.com','user',NULL,1,22,'软件工程学生',NULL,0,1,'2026-04-17 10:00:00','2026-05-19 20:51:21'),(32,'songjuan','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','宋娟','13800001018','songjuan@test.com','user',NULL,2,21,'社会工作学生',NULL,0,1,'2026-04-18 11:00:00','2026-05-19 20:51:21'),(33,'fengtao','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','冯涛','13800001019','fengtao@test.com','user',NULL,1,24,'建筑学学生',NULL,0,1,'2026-04-19 08:00:00','2026-05-19 20:51:21'),(34,'panlin','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','潘琳','13800001020','panlin@test.com','user',NULL,2,20,'护理学学生',NULL,0,1,'2026-04-20 09:00:00','2026-05-19 20:51:21');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'mental_health'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-19 22:57:53
