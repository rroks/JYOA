var pointLion = function(){
		/***
		 * 选择一个组织结构方法
		 * callback回调函数。
		 * orgid：父级单位，不传或者传null则为子公司id。
		 * ifAllChild：是否查询子级所有
		 */
		var selectOneOrgNode;//机构数据
		var selectOneOrg = function(callback,orgid,ifAllChild,ifOpen,ifOnlyLeaf){
			var para = "1=1";
			if(ifAllChild){
				para = para +"&ifAllChild="+ifAllChild;
			}
			if(ifOpen){
				para = para +"&ifOpen="+ifOpen;
			}
			if(ifOnlyLeaf){
				para = para +"&ifOnlyLeaf="+ifOnlyLeaf;
			}
			if(orgid){
				para = para +"&orgid="+encodeURI(orgid);
			}
			selectOneOrgNode={}
			layer.open({
				  type: 2,
				  title: false, //不显示标题栏
				  area: ['300px', '550px'],
				  shade: 0.8,
				  id: 'selectOneOrg', //设定一个id，防止重复弹出
				  resize: false,
				  closeBtn: false,
				  isOutAnim : false , 
				  btn: ['确定', '取消'], 
				  btnAlign: 'c',
				  content: ctx+'/admin/sys/org/getSelectOneOrgPage?'+para,
				  success: function(layero){
					  
				  },
				  yes: function(){
					  if( callback != null ){
						  callback(selectOneOrgNode);
					  }
					  layer.closeAll();
				  }
			});
			
		};
		//获取选择好的机构数据---提供给弹出层调用
		var  setOneOrgNode = function(treeNode){
			selectOneOrgNode = treeNode;
		};
		/***
		 * 选择多个组织结构方法
		 */
		var selectManyOrgNode;//机构数据
		var selectManyOrg = function(callback){
			selectManyOrgNode={};
			layer.open({
			  type: 2,
			  title: false, //不显示标题栏
			  area: ['300px', '550px'],
			  shade: 0.8,
			  id: 'selectManyOrg', //设定一个id，防止重复弹出
			  resize: false,
			  closeBtn: false,
			  isOutAnim : false , 
			  btn: ['确定', '取消'], 
			  btnAlign: 'c',
			  content: ctx+'/admin/sys/org/getSelectManyOrgPage',
			  success: function(layero){
				  
			  },
			  yes: function(){
				  if( callback != null ){
					  callback(selectManyOrgNode);
				  }
				  layer.closeAll();
			  }
			});
		};
		/***
		 * 选择多个组织结构方法
		 */
		var editManyOrgNode;//机构数据
		var editManyOrg = function(callback,orgid){
			editManyOrgNode={};
			layer.open({
				type: 2,
				title: false, //不显示标题栏
				area: ['300px', '550px'],
				shade: 0.8,
				id: 'editManyOrg', //设定一个id，防止重复弹出
				resize: false,
				closeBtn: false,
				isOutAnim : false ,
				btn: ['确定', '取消'],
				btnAlign: 'c',
				content: ctx+'/admin/sys/org/geteditManyOrgPage?orgid='+orgid,
				success: function(layero){
					editManyOrgNode = window[layero.find('iframe')[0]['name']];
				},
				yes: function(){
					var nodes = editManyOrgNode.checkNode1();
					if( callback != null ){
						callback(nodes);
					}
					layer.closeAll();
				}
			});
		};
		//获取选择好的机构数据---提供给弹出层调用
		var  seteditManyOrgNode = function(treeNode){
			editManyOrgNode = treeNode;
		};



	/***
		 * 选择多个组织结构方法(数据权限使用)
		 */
		var selectManyOrgNode4Per;//机构数据
		var selectManyOrg4Per = function(callback,userid,groupid){
			selectManyOrgNode4Per={};
			layer.open({
			  type: 2,
			  title: false, //不显示标题栏
			  area: ['300px', '550px'],
			  shade: 0.8,
			  id: 'selectManyOrg4Per', //设定一个id，防止重复弹出
			  resize: false,
			  closeBtn: false,
			  isOutAnim : false , 
			  btn: ['确定', '取消'], 
			  btnAlign: 'c',
			  content: ctx+'/admin/sys/org/getSelectManyOrgPage4Per?userid='+userid+'&groupid='+groupid,
			  success: function(layero){

			  },
			  yes: function(){
				  if( callback != null ){
					  callback(selectManyOrgNode4Per);
				  }
				  layer.closeAll();
			  }
			});
		};
		
		
		//获取选择好的机构数据---提供给弹出层调用
		var  setManyOrgNode = function(treeNode){
			 selectManyOrgNode = treeNode;
		};
		
		//获取选择好的机构数据---提供给弹出层调用
		var  setManyOrgNode4Per = function(treeNode){
			 selectManyOrgNode4Per = treeNode;
		};
		/***
		 * 打开选择角色
		 */
		var selectOneRoleNode;//角色数据
		function selectOneRole(callback){
					selectOneRoleNode={};//初始化数据
					var giveUserRoleIframe;
					var index = layer.open({
						  type: 2,
						  title: false, //不显示标题栏
						  area: ['370px', '650px'],
						  shade: 0.8,
						  id: 'selectOneRole', //设定一个id，防止重复弹出
						  resize: false,
						  closeBtn: false,
						  isOutAnim : false , 
						  btn: ['确定', '取消'], 
						  btnAlign: 'c',
						  content: ctx+'/admin/sys/role/getSelectOneRolePage',
						  success: function(layero){
							  
						  },
						  yes: function(){
							  if( callback != null ){
								  callback(selectOneRoleNode);
							  }
							  layer.closeAll();
						  }
					});
		}
		//获取选择好的角色数据---提供给弹出层调用
		var  setOneRoleNode = function(treeNode){
			selectOneRoleNode = treeNode;
		};
		/***
		 * 公用弹出提醒
		 * msg:弹出默认提醒
		 * type:类型
		 * size：大小
		 * callback：回调函数
		 * data:总数据。可根据类型，重写msg。
		 */
		var alertMsg = function(msg,type,size,callback,data){
			if(data){
				if(!data.success){
					var errorType = data.data.type;
					if(errorType&&errorType=="errorCol"){
						var errorCol = data.data.errorCol;
						var name = $("input[name$='"+errorCol+"']").parents(".form-group").find(".control-label").text();
						msg = "["+ name +"]"+ msg;
					}
				}
			}
			var t = "mint";//默认颜色
			var s = "small";
			if(type){
				t = type;
			}
			if(size){
				s = size;
			}
			bootbox.dialog({
	            buttons: {
	            	ok: {
	                    label: '确定',
	                    className: "btn-"+t,
	                    callback : function(){
	    	            	if(callback!=null){
	    	    				callback();
	    	    			}
	    	            },
	                }  
	            },  
	            message: msg,  
	            title : '提示信息',
	            size: s,
	            animateIn: 'flipInX',
	            animateOut : 'flipOutX' 
	        });  
		};
		
		
		/***
		 * 公用弹出图片
		 * type:类型
		 * size：大小
		 * callback：回调函数
		 * data:总数据。可根据类型，重写msg。
		 */
		var openImg = function(imgUri){
			if(imgUri){
						var name = $("img[src$='"+errorCol+"']").parents(".form-group").find(".control-label").text();
						msg = "["+ name +"]"+ msg;
			}
			var t = "mint";//默认颜色
			var s = "small";
			if(type){
				t = type;
			}
			if(size){
				s = size;
			}
		};
		/***
		 * 公用弹出提醒
		 */
		var confimMsg = function(msg,size,callback){
			var s = "small";
			if(size){
				s = size;
			}
			bootbox.dialog({
	            buttons: {
	            	cancel : {
	                	label: '取消',
	                    className: "btn-default",
	                    callback : function(){
	    	            	
	    	            }
	                },
	            	ok: {
	                    label: '确定',
	                    className: "btn-mint",
	                    callback : function(){
	    	            	if(callback!=null){
	    	    				callback();
	    	    			}
	    	            }
	                }
	                
	            },  
	            message: msg,  
	            title : '向您确认',
	            size: s,
	            animateIn: 'swing',
	            animateOut : 'hinge'
	        });  
		};
		/***
		 * 弹出即时聊天页面
		 */
		var chatId;
		var openChat = function(uid){
			chatId = layer.open({
				  type: 2,
				  title: false, //不显示标题栏
				  area: ['500px', '552px'],
				  shade: 0.8,
				  id: 'openChat', //设定一个id，防止重复弹出
				  resize: false,
				  closeBtn: false,
				  isOutAnim : false , 
				  btnAlign: 'c',
				  content: ctx+'/admin/sys/chat/getChatPage?id='+uid,
				  success: function(layero){
					  
				  }
				});
		};
		var closeChat = function(){
			layer.close(chatId);
		};
		/***
		 * 通用单个文件上传
		 */
		var initUploader = function(url,callback){
			if(url==null||url.length==0){
				url = '/admin/upload/upload';
			}
			var uploader = WebUploader.create({
			    // 选完文件后，是否自动上传。
			    auto: true,
			    // swf文件路径
			    swf: ctx+'/common/plugins/webuploader/Uploader.swf',
			    // 文件接收服务端。
			    server: ctx+url,
			    // 内部根据当前运行是创建，可能是input元素，也可能是flash.
			    pick: '#filePicker'
			    //,
//			    accept: {// 只允许选择图片文件。
//			        title: 'Images',
//			        extensions: 'gif,jpg,jpeg,bmp,png',
//			        mimeTypes: 'image/*'
//			    }
			});
			//上传成功，添加缩略图，和添加路径参数
			uploader.on( 'uploadSuccess', function( file ,data) {
				if(callback!=null){
    				callback(data);
    			}
			});
			// 文件上传失败，显示上传出错。
			uploader.on( 'uploadError', function( file ) {
			    alert("上传出错");
			});

			// 完成上传完了，成功或者失败，先删除进度条。
			uploader.on( 'uploadComplete', function( file ) {
				
			});
		};
		/***
		 * 打开公共流程历史任务列表页面
		 */
		var openTaskHisListPage = function(insid){
			layer.open({
				  type: 2,
				  title: false, //不显示标题栏
				  area: ['890px', '500px'],
				  shade: 0.8,
				  id: 'taskHisListPage', //设定一个id，防止重复弹出
				  resize: false,
				  closeBtn: false,
				  isOutAnim : false , 
				  btnAlign: 'c',
				  content: ctx+'/admin/oa/workflow/getWorkFlowHis?insid='+insid,
				  success: function(layero){
					  
				  }
			});
		}
		/***
		 * 打开公共自定义审批流程历史任务列表页面
		 */
		var openCustomTaskHisListPage = function(insid){
			layer.open({
				type: 2,
				title: false, //不显示标题栏
				area: ['890px', '500px'],
				shade: 0.8,
				id: 'CustomtaskHisListPage', //设定一个id，防止重复弹出
				resize: false,
				closeBtn: false,
				isOutAnim : false ,
				btnAlign: 'c',
				content: ctx+'/admin/oa/CustomFlow/Case/openTaskHisListPage?caseid='+insid,
				success: function(layero){

				}
			});
		}

		/***
		 * 打开公共的附件上传界面
		 */
		var openBusinessAttachmentPage = function(busid,view,callback){
			layer.open({
				  type: 2,
				  title: false, //不显示标题栏
				  area: ['1000px', '550px'],
				  shade: 0.8,
				  id: 'taskHisListPage', //设定一个id，防止重复弹出
				  resize: false,
				  closeBtn: false,
				  isOutAnim : false , 
				  btnAlign: 'c',
				  btn: ['确定', '取消'], 
				  content: ctx+'/admin/sys/attachment/getBusinessUploadListPage?busid='+busid+'&view='+view,
				  success: function(layero){
					  
				  },
				  yes: function(){
					  layer.closeAll();
				  },
				  end: function(){
					  if( callback != null ){
						  callback();
					  }
				  }
			});
		}
		/***
		 * 选择多个人的方法,抄送使用
		 */
		var selectManyUser4CCIframe;
		var selectManyUser4CC = function(paraStr,callback){
			if(!paraStr){
				paraStr="";
			}else{
				
			}
			layer.open({
			  type: 2,
			  title: false, //不显示标题栏
			  area: ['1100px', '550px'],
			  shade: 0.8,
			  id: 'selectManyUser4CC', //设定一个id，防止重复弹出
			  resize: false,
			  closeBtn: false, 
			  isOutAnim : false , 
			  btn: ['添加所选', '确定'], 
			  btnAlign: 'c',
			  content: ctx+'/admin/sys/user/openSelectManyUserPage4CC?'+paraStr,
			  success: function(layero){
				  selectManyUserIframe4CC = window[layero.find('iframe')[0]['name']];
				  
			  },
			  yes: function(){
				  selectManyUserIframe4CC.addSelectUser();//添加人
			  },
			  btn2:function(){
				  var data = selectManyUserIframe4CC.selectMankUserOK();
				  if( callback != null ){
					  callback(data);
				  }
				  layer.closeAll();
			  }
			});
		};
		
		
		/***
		 * 选择多个人的方法
		 */
		var selectManyUserIframe;
		var selectManyUser = function(paraStr,callback){
			if(!paraStr){paraStr="";}
			layer.open({
			  type: 2,
			  title: false, //不显示标题栏
			  area: ['1100px', '550px'],
			  shade: 0.8,
			  id: 'selectManyUser', //设定一个id，防止重复弹出
			  resize: false,
			  closeBtn: false,
			  isOutAnim : false , 
			  btn: ['添加所选', '确定'], 
			  btnAlign: 'c',
			  content: ctx+'/admin/sys/user/openSelectManyUserPage?'+paraStr,
			  success: function(layero){
				  selectManyUserIframe = window[layero.find('iframe')[0]['name']];
			  },
			  yes: function(){
				  selectManyUserIframe.addSelectUser();//添加人
			  },
			  btn2:function(){
				  var data = selectManyUserIframe.selectMankUserOK();
				  if( callback != null ){
					  callback(data);
				  }
				  layer.closeAll();
			  }
			});
		};
		/***
		 * 选择单个人的方法
		 */
		var selectOneUserIframe;
		var selectOneUser = function(orgid,callback){
			if(!orgid){orgid="";}
			var index = layer.open({
			  type: 2,
			  title: false, //不显示标题栏
			  area: ['1100px', '550px'],
			  shade: 0.8,
			  id: 'selectOneUser', //设定一个id，防止重复弹出
			  resize: false,
			  closeBtn: false,
			  isOutAnim : false , 
			  btn: ['确定', '取消'], 
			  btnAlign: 'c',
			  content: ctx+'/admin/sys/user/openSelectOneUserPage?orgid='+orgid,
			  success: function(layero){
				  selectManyUserIframe = window[layero.find('iframe')[0]['name']];
			  },
			  yes: function(){
				  var data = selectManyUserIframe.selectMankUserOK();
				  if( callback != null ){
					  callback(data);
				  }
				  if(data){
					  parent.layer.close(index);
				  }
			  }
			});
		};

		var selectOneUserUseRoleIframe;
		var selectOneUserUseRole = function(paraStr,callback){
			if(!paraStr){paraStr="";}
			layer.open({
			  type: 2,
			  title: false, //不显示标题栏
			  area: ['1000px', '550px'],
			  shade: 0.8,
			  id: 'selectManyUser', //设定一个id，防止重复弹出
			  resize: false,
			  closeBtn: false,
			  isOutAnim : false , 
			  btn: ['确定', '取消'], 
			  btnAlign: 'c',
			  content: ctx+'/admin/sys/user/openSelectOneUserUseRolePage?roleKey='+paraStr,
			  success: function(layero){
				selectOneUserUseRoleIframe = window[layero.find('iframe')[0]['name']];
			  },
			  yes: function(){
				  var data = selectOneUserUseRoleIframe.selectMankUserOK();
				  if( callback != null ){
					  callback(data);
				  }
				  if(data){
					  parent.layer.closeAll();
				  }
			  }
			});
		};
		return {
			selectOneOrg : selectOneOrg,//选择一个单位
			setOneOrgNode : setOneOrgNode,//选择一个单位
			setManyOrgNode4Per : setManyOrgNode4Per,
			selectManyOrg : selectManyOrg,//选择多个单位
			selectManyOrg4Per : selectManyOrg4Per,//数据权限用的，选择多个单位
			setManyOrgNode : setManyOrgNode,//选择多个单位
			editManyOrg : editManyOrg,//编辑单位
			seteditManyOrgNode : seteditManyOrgNode,//编辑单位
			selectOneRole : selectOneRole,//选择一个角色
			setOneRoleNode : setOneRoleNode,//选择一个角色
			alertMsg : alertMsg ,//通用提醒框
			confimMsg : confimMsg,//通用确认
			openChat : openChat,//打开聊天
			closeChat : closeChat,//关闭聊天
			initUploader : initUploader,//初始化通用上传按钮
			openTaskHisListPage : openTaskHisListPage,//打开流程的通用流转历史
			openCustomTaskHisListPage :openCustomTaskHisListPage,//打开自定义流程的通用流转历史
			openBusinessAttachmentPage : openBusinessAttachmentPage,//打开业务通用附件上传
			selectManyUser:selectManyUser,//选择多个人
			selectManyUser4CC:selectManyUser4CC,
			selectOneUser :selectOneUser,//选择单个人
			selectOneUserUseRole :selectOneUserUseRole,//选择单个人
		};
}();

//获取选择的节点
function getCheckedNodes(){
	var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
	var nodes = treeObj.getCheckedNodes(true);
	return nodes;
}

/**
 * 扩展Array方法, 去除数组中空白数据
 */
Array.prototype.notempty = function() {
    var arr = [];
    this.map(function(val, index) {
        //过滤规则为，不为空串、不为null、不为undefined，也可自行修改
        if (val !== "" && val != undefined) {
            arr.push(val);
        }
    });
    return arr;
}