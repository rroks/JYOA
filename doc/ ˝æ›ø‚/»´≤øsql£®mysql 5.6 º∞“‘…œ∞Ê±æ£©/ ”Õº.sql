-- ----------------------------
-- View structure for v_candidate_candidate
-- ----------------------------
DROP VIEW IF EXISTS `v_candidate_candidate`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER  VIEW `v_candidate_candidate` AS SELECT TASK_ID_, USER_ID_ USER_ID
                    FROM ACT_RU_IDENTITYLINK I, ACT_RU_TASK T
                      WHERE TASK_ID_ IS NOT NULL
                        AND USER_ID_ IS NOT NULL
                        AND I.TASK_ID_ = T.ID_
                        AND T.ASSIGNEE_ IS NULL
                        AND TYPE_ = 'candidate' ;

-- ----------------------------
-- View structure for v_candidate_group
-- ----------------------------
DROP VIEW IF EXISTS `v_candidate_group`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER  VIEW `v_candidate_group` AS SELECT TASK_ID_,M.USER_ID_ USER_ID
                    FROM ACT_RU_IDENTITYLINK I, ACT_RU_TASK T,act_id_membership M
                      WHERE 
                        I.TASK_ID_ = T.ID_
                        AND TASK_ID_ IS NOT NULL
                        AND I.USER_ID_ IS NULL
                        AND T.ASSIGNEE_ IS NULL
                        AND TYPE_ = 'candidate' 
                        AND M.GROUP_ID_ = I.GROUP_ID_ ;

-- ----------------------------
-- View structure for v_candidate_z_distinct
-- ----------------------------
DROP VIEW IF EXISTS `v_candidate_z_distinct`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost`  VIEW `v_candidate_z_distinct` AS (select * from v_candidate_candidate)
UNION 
(select * from v_candidate_group) ;

-- ----------------------------
-- View structure for v_tasklist
-- ----------------------------
DROP VIEW IF EXISTS `v_tasklist`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER  VIEW `v_tasklist` AS SELECT A.ID_ AS TASKID,
       A.PROC_INST_ID_ AS INSID,
       A.TASK_DEF_KEY_ AS TASKDEFKEY,
       D.KEY_ AS DEFKEY,
       D.NAME_ AS DEFNAME,
       A.NAME_ AS TASKNAME,
       A.ASSIGNEE_ AS ASSIGNEE,
       I.USER_ID CANDIDATE,
       A.PROC_DEF_ID_ AS DEFID,
       A.DELEGATION_ AS DELEGATIONID,
       A.DESCRIPTION_ AS DESCRIPTION,
       date_format(A.CREATE_TIME_,'%Y-%m-%d %H:%i:%s') AS CREATETIME,
       date_format(A.DUE_DATE_,'%Y-%m-%d %H:%i:%s') AS DUEDATE
  FROM ACT_RU_TASK A
  LEFT JOIN V_CANDIDATE_Z_DISTINCT I
    ON A.ID_ = I.TASK_ID_ 
   LEFT JOIN ACT_RE_PROCDEF D 
   ON A.PROC_DEF_ID_ = D.ID_ ;
