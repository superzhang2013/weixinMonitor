package com.zh.weixinmonitor;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.zh.data.DataApiFactory;
import com.zh.data.entity.DBGroupLastChatLog;
import com.zh.data.entity.DBUser;
import com.zh.data.server.QEncodeUtil;
import com.zh.data.server.entity.ChatBean;
import com.zh.data.server.entity.UploadChatBean;
import com.zh.data.server.entity.UserBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanghong on 2017/6/28.
 */

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";

    private String ChatRecord = "";

    private String mCurrentChatName = "";

    private AccessibilityNodeInfo lastCheckNodeInfo;

    private AccessibilityNodeInfo bottomNodeInfo;

    private AccessibilityNodeInfo topNodeInfo;

    private AccessibilityNodeInfo currentChatPageTopNode;

    private AccessibilityNodeInfo currentChatPageBottomNode;

    private List<AccessibilityNodeInfo> listViewNodes = new ArrayList<>();

    //用于保存当前页面已经处理过的会话名，即List<GroupName>
    private List<String> currentPageAlreadyHandleGroups = new ArrayList<>();

    private List<ChatBean> sendChatBeans = new ArrayList<>();

    private ChatBean lastChatBean;

    private static final int MAX_SCROLL_PAGE_NUMBER = 3;

    private int scrollPageCount = 0;

    private int findDBChatTimePageCount = 0;

    private static final int ACTION_TYPE_FIND_LAST_CHAT_LOG = 1;

    private static final int ACTION_TYPE_SEND_RECENT_CHAT_LOG = 2;

    private static final int ACTION_TYPE_GET_LAST_CHAT_LOG_TIME = 3;

    private static final int ACTION_TYPE_UN_START = -1;

    private int currentActionType = -1;

    private ChatBean currentChatBean;

    //数据库中保存的上一次的最终聊天记录
    private ChatBean dbLastChatBean;

    //本次最终需要发送的chatbean
    private ChatBean currentLastChatBean;

    //当前自上而下的遍历任务是否结束
    private boolean isTaskFinished = true;

    //聊天详情页面的遍历及发送数据过程是否结束
    private boolean isChatLogFinished = false;

    //判断聊天详情页是否由获取用户信息界面返回或自循环，防止过滤重复的异常返回出现
    private boolean isScrollFromUser = false;

    //判断是否从详情页出来 从而判断是否继续遍历会话
    private boolean isScrollFromChatDetail = false;

    //判断本次聊天消息是否已经标记为发送
    private boolean isCurrentChatLogSend = false;

    private int currentChatPointCount;

    private String currentCheckUserName;

    private String currentCheckGroupName;

    private long currentLastChatLogTime;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(this.toString(), event.toString());
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: {

                //获取当前聊天页面的根布局
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                Log.i(TAG, "execute screen onAccessibilityEvent TYPE_WINDOW_CONTENT_CHANGED ");
                if (rootNode != null) {
                    List<AccessibilityNodeInfo> userWeiXinNameNodes = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/mh");
                    if (userWeiXinNameNodes != null && userWeiXinNameNodes.size() > 0) {
                        getUserInfo(rootNode, userWeiXinNameNodes.get(0));
                    }
                    List<AccessibilityNodeInfo> pageList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a3e");
                    if (pageList != null && pageList.size() > 0) {
                        //进入详细聊天目录
                        isTaskFinished = false;
                        isChatLogFinished = false;
                        isCurrentChatLogSend = false;
                        isScrollFromUser = true;
                        getChatDetailList(rootNode);
                    } else {
                        //遍历当前对话列表 并进行消息记录遍历
                        isScrollFromChatDetail = true;
                        int unReadMessageCount = getMessageCount(rootNode);
                        if (unReadMessageCount > 0 && isTaskFinished) {
                            topNodeInfo = null;
                            isTaskFinished = false;
                            goReadyPosition(rootNode);
                            getChatList(rootNode);
                        } else {
                            getChatList(rootNode);
                        }
                    }
                }
            }
            break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getUserInfo(AccessibilityNodeInfo rootNode, AccessibilityNodeInfo userNameNode) {
        if (userNameNode != null) {
            String userName = userNameNode.getText().toString().trim();
            if (!TextUtils.isEmpty(userName) && userName.equals(mCurrentChatName)) {
                List<AccessibilityNodeInfo> userWeiXinNames = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/aeq");
                if (userWeiXinNames != null && userWeiXinNames.size() > 0) {
                    String weixinName = userWeiXinNames.get(0).getText().toString();
                    Toast.makeText(this, "已经对比用户昵称，准备获取的用户名为 " + userWeiXinNames.get(0).getText(), Toast.LENGTH_SHORT).show();
                    DataApiFactory.getInstance().getIDBApi().saveDBUser(currentCheckGroupName, mCurrentChatName, weixinName);
                    if (currentChatBean != null && currentChatBean.chatBean != null && currentChatBean.chatBean.weixinNickName.equals(userName)) {
                        currentChatBean.chatBean.userWeiXinName = weixinName;
                    }
                } else {
                    DataApiFactory.getInstance().getIDBApi().saveDBUser(currentCheckGroupName, mCurrentChatName, "");
                    if (currentChatBean != null && currentChatBean.chatBean != null && currentChatBean.chatBean.weixinNickName.equals(userName)) {
                        currentChatBean.chatBean.userWeiXinName = "";
                    }
                }
                try {
                    List<AccessibilityNodeInfo> backNodeInfo = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/h3");
                    backNodeInfo.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    isScrollFromUser = true;
                } catch (Exception ex) {

                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int getMessageCount(AccessibilityNodeInfo rootNode) {
        int count = 0;
        List<AccessibilityNodeInfo> currentScreenNodeMessageInfos = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bue");
        if (currentScreenNodeMessageInfos.size() > 0) {
            AccessibilityNodeInfo pointOneNodeInfo = currentScreenNodeMessageInfos.get(0);
            if (pointOneNodeInfo != null && pointOneNodeInfo.getChild(0) != null) {
                AccessibilityNodeInfo pointOneChildNodeInfo = pointOneNodeInfo.getChild(0);
                if (pointOneChildNodeInfo != null) {
                    List<AccessibilityNodeInfo> countPointNodes = pointOneChildNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/buf");
                    if (countPointNodes != null && countPointNodes.size() > 0) {
                        count = Integer.parseInt(countPointNodes.get(0).getText().toString());
                    }
                }
            }
        }
        return count;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getChatList(AccessibilityNodeInfo rootNode) {
        //添加任务队列
        Log.i(TAG, "execute screen getChatList ready to find task list ");
        listViewNodes = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bny");
        List<AccessibilityNodeInfo> currentScreenNodeInfos = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/agu");
        int currentScreenNodesCount = currentScreenNodeInfos.size();
        AccessibilityNodeInfo preClickNodeInfo = null;
        if (currentScreenNodeInfos.size() > 0) {
            if (!isTaskFinished && isScrollFromChatDetail && (lastCheckNodeInfo == null || lastCheckNodeInfo.getCollectionItemInfo().getRowIndex() == 0 ||
                    getSourceNodeId(lastCheckNodeInfo) != getSourceNodeId(currentScreenNodeInfos.get(currentScreenNodesCount - 1)))) {
                //遍历当前屏幕的聊天列表，并根据泡泡数判断是否进去详细聊天界面
                Log.i(TAG, "execute screen getTaskList execute find task ");

                int i = 0;
                for (; i < currentScreenNodesCount; i++) {
                    AccessibilityNodeInfo currentInfoNode = currentScreenNodeInfos.get(i);
                    List<AccessibilityNodeInfo> groupNameNodes = currentInfoNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/agw");
                    AccessibilityNodeInfo childFirstNode = currentInfoNode.getChild(0);
                    if (childFirstNode != null) {
                        String groupName = currentCheckGroupName;
                        if (groupNameNodes != null && groupNameNodes.size() > 0) {
                            groupName = groupNameNodes.get(0).getText().toString();
                        }
                        Log.i(TAG, "execute screen executeTask currentPageAlreadyHandleGroups is have group " + currentPageAlreadyHandleGroups.contains(groupName));
                        if ("android.widget.TextView".equals(childFirstNode.getClassName())) {
                            //判断当前会话列表的泡泡数是否大于0 且 是否已经在当前页面遍历过
                            if (Integer.parseInt(childFirstNode.getText().toString()) > 0 && currentInfoNode != lastCheckNodeInfo
                                    && !currentPageAlreadyHandleGroups.contains(groupName)) {
                                preClickNodeInfo = currentInfoNode;
                                break;
                            }
                        }
                    }
                }

                if (preClickNodeInfo != null) {
                    executeTask(preClickNodeInfo);
                    isScrollFromChatDetail = true;
                    lastCheckNodeInfo = preClickNodeInfo;
                } else {
                    isScrollFromChatDetail = false;
                }
            }

            AccessibilityNodeInfo listViewNode = listViewNodes.get(0);

            if (preClickNodeInfo == null && !isTaskFinished && listViewNode != null && "android.widget.ListView".equals(listViewNode.getClassName())) {
                //一旦屏幕已经滚动，当前遍历的任务数全部清空
                Log.i(TAG, "execute screen getChatList currentPageAlreadyHandleGroups clear " + currentPageAlreadyHandleGroups.toString());
                currentPageAlreadyHandleGroups.clear();

                AccessibilityNodeInfo currentBottomNode = getBottomNode(rootNode);

                Log.i(TAG, "execute screen getChatList execute ACTION_SCROLL_FORWARD task " + isTaskFinished + " and is bottom "
                        + (bottomNodeInfo == null || getSourceNodeId(bottomNodeInfo) != getSourceNodeId(currentBottomNode)));
                if (bottomNodeInfo == null || getSourceNodeId(bottomNodeInfo) != getSourceNodeId(currentBottomNode)) {
                    isTaskFinished = false;
                    bottomNodeInfo = currentBottomNode;
                    Log.i(TAG, "execute screen getChatList performAction ACTION_SCROLL_FORWARD before ");
                    topNodeInfo = null;
                    listViewNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                    Log.i(TAG, "execute screen getChatList performAction ACTION_SCROLL_FORWARD after ");

                } else {
                    isTaskFinished = true;
                }
            }
            Log.i(TAG, "execute screen getChatList execute goReadyPosition task " + isTaskFinished);
            if (isTaskFinished) {
                goReadyPosition(rootNode);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getTaskList(AccessibilityNodeInfo rootNode) {
        Log.i(TAG, "execute screen getTaskList execute find task ");
        List<AccessibilityNodeInfo> record = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/agu");
        AccessibilityNodeInfo preClickNodeInfo = null;
        int recordSize = record.size();
        if (recordSize > 0) {
            if (bottomNodeInfo != null && record.get(recordSize - 1) == bottomNodeInfo) {
                return;
            }
            int i = 0;
            for (; i < recordSize; i++) {
                AccessibilityNodeInfo currentInfoNode = record.get(i);
                List<AccessibilityNodeInfo> groupNameNodes = currentInfoNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/agw");
                AccessibilityNodeInfo childFirstNode = currentInfoNode.getChild(0);
                if (childFirstNode != null) {
                    String groupName = currentCheckGroupName;
                    if (groupNameNodes != null && groupNameNodes.size() > 0) {
                        groupName = groupNameNodes.get(0).getText().toString();
                    }
                    Log.i(TAG, "execute screen executeTask currentPageAlreadyHandleGroups is have group " + currentPageAlreadyHandleGroups.contains(groupName));
                    if ("android.widget.TextView".equals(childFirstNode.getClassName())) {
                        //判断当前会话列表的泡泡数是否大于0 且 是否已经在当前页面遍历过
                        if (Integer.parseInt(childFirstNode.getText().toString()) > 0 && currentInfoNode != lastCheckNodeInfo
                                && !currentPageAlreadyHandleGroups.contains(groupName)) {
                            preClickNodeInfo = currentInfoNode;
                            break;
                        }
                    }
                }
            }
//            bottomNodeInfo = record.get(recordSize - 1);
        }

        if (preClickNodeInfo != null) {
            executeTask(preClickNodeInfo);
            isScrollFromChatDetail = true;
            lastCheckNodeInfo = preClickNodeInfo;
        } else {
            isScrollFromChatDetail = false;
        }

//        if (listViewNodes != null && !isTaskFinished) {
//            AccessibilityNodeInfo listViewNode = listViewNodes.get(0);
//            Log.i(TAG, "execute screen getTaskList performAction ACTION_SCROLL_FORWARD before ");
//            topNodeInfo = null;
//            listViewNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
//            Log.i(TAG, "execute screen getTaskList performAction ACTION_SCROLL_FORWARD after ");
//            try{
//                //向下滚动后，暂停两秒钟
//                Thread.sleep(2000);
//            }catch (Exception ex){
//
//            }
//        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void executeTask(AccessibilityNodeInfo selectClickNode) {
        if (selectClickNode != lastCheckNodeInfo) {
            AccessibilityNodeInfo firstChildNode = selectClickNode.getChild(0);
            if (firstChildNode != null) {
                Toast.makeText(this, "有新的消息数 " + firstChildNode.getText(), Toast.LENGTH_SHORT).show();
                currentChatPointCount = Integer.valueOf(firstChildNode.getText().toString());
            }
            List<AccessibilityNodeInfo> groupNodeInfos = selectClickNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/agw");
            if (groupNodeInfos != null && groupNodeInfos.size() > 0) {
                currentCheckGroupName = groupNodeInfos.get(0).getText().toString();
                Log.i(TAG, "execute screen executeTask currentPageAlreadyHandleGroups add group " + currentCheckGroupName);
                currentPageAlreadyHandleGroups.add(currentCheckGroupName);
            }
            Log.i(TAG, "current execute thread is " + Thread.currentThread().getName());
            Log.i(TAG, "execute screen executeTask performAction ACTION_CLICK before ");
            selectClickNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            Log.i(TAG, "execute screen executeTask performAction ACTION_CLICK after ");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private AccessibilityNodeInfo getBottomNode(AccessibilityNodeInfo rootNode) {
        List<AccessibilityNodeInfo> listViewNodes = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/agu");
        int currentScreenNodesCount = listViewNodes.size();
        return listViewNodes.get(currentScreenNodesCount - 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private AccessibilityNodeInfo getTopNodeInfo(AccessibilityNodeInfo rootNode) {
        List<AccessibilityNodeInfo> listViewNodes = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/agu");
        return listViewNodes.get(0);
    }

    @Override
    public void onInterrupt() {

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void goReadyPosition(AccessibilityNodeInfo rootNode) {
        List<AccessibilityNodeInfo> listViewNodes = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bny");
        AccessibilityNodeInfo listViewNode = listViewNodes.get(0);
        if (listViewNode != null && "android.widget.ListView".equals(listViewNode.getClassName()) && listViewNode.isScrollable()) {
            AccessibilityNodeInfo accessibilityNodeInfo = getTopNodeInfo(listViewNode);

            Log.i(TAG, "execute screen goReadyPosition execute ACTION_SCROLL_BACKWARD task  is top "
                    + (topNodeInfo == null || getSourceNodeId(topNodeInfo) != getSourceNodeId(accessibilityNodeInfo)));

            if (topNodeInfo == null || getSourceNodeId(topNodeInfo) != getSourceNodeId(accessibilityNodeInfo)) {
                topNodeInfo = accessibilityNodeInfo;
                Log.i(TAG, "execute screen goReadyPosition performAction ACTION_SCROLL_BACKWARD before ");
                listViewNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                Log.i(TAG, "execute screen goReadyPosition performAction ACTION_SCROLL_BACKWARD after ");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getChatDetailList(AccessibilityNodeInfo rootNode) {
        GetChatName(rootNode);

        getChatInfo(rootNode);
    }

    /**
     * 遍历所有控件，找到头像Imagview，里面有对联系人的描述
     */
    private void GetChatName(AccessibilityNodeInfo node) {
        if (node == null) {
            return;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo node1 = node.getChild(i);
            if (node1 == null) {
                continue;
            }
            if ("android.widget.ImageView".equals(node1.getClassName()) && node1.isClickable()) {
                //获取聊天对象,这里两个if是为了确定找到的这个ImageView是头像的
                if (!TextUtils.isEmpty(node1.getContentDescription())) {
                    mCurrentChatName = node1.getContentDescription().toString();
                    if (mCurrentChatName.contains("头像")) {
                        mCurrentChatName = mCurrentChatName.replace("头像", "");
                    }
                }

            }
            GetChatName(node1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean findDBLastChatLogTime(AccessibilityNodeInfo rootNode, AccessibilityNodeInfo listviewNode, int childCount) {
        if (findDBChatTimePageCount <= MAX_SCROLL_PAGE_NUMBER) {
            findDBChatTimePageCount++;
        } else {
            //查找当前页的第一条作为dbLastChatBean
            return true;
        }
        //之所以查找最近一条记录时间，说明当前dbLastChatBean已经在页面当中，或者在上一个页面当中
        if (dbLastChatBean != null) {
            String searchContent = dbLastChatBean.sendContent;
            List<AccessibilityNodeInfo> childChatNodes = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/im");
            boolean isExistInThisPage = false;
            for (int i = 0; i < childChatNodes.size(); i++) {
                String childChatInfo = childChatNodes.get(i).getText().toString();
                if (searchContent.equals(childChatInfo)) {
                    isExistInThisPage = true;
                    break;
                }
            }
            if (isExistInThisPage) {
                String tempTimeString = "";
                int childChatCount = listviewNode.getChildCount();
                for (int i = 0; i < childChatCount; i++) {
                    AccessibilityNodeInfo childNode = listviewNode.getChild(i);
                    List<AccessibilityNodeInfo> detailChildChatTime = childNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/t");
                    List<AccessibilityNodeInfo> detailChildChat = childNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/im");
                    if (detailChildChatTime != null && detailChildChatTime.size() > 0) {
                        tempTimeString = detailChildChatTime.get(0).getText().toString();
                    }
                    if (detailChildChat != null && detailChildChat.size() > 0) {
                        String detailContent = detailChildChat.get(0).getText().toString();
                        if (searchContent.equals(detailContent)) {
                            if (!TextUtils.isEmpty(tempTimeString)) {
                                currentLastChatLogTime = TimeUtils.getDayTimeOfWeiXin(tempTimeString);
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }
                }
            } else {
                List<AccessibilityNodeInfo> childTimeNodes = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/t");
                if (childTimeNodes != null && childTimeNodes.size() > 0) {
                    int childTimeCount = childTimeNodes.size();
                    AccessibilityNodeInfo accessibilityNodeInfo = childTimeNodes.get(childTimeCount - 1);
                    currentLastChatLogTime = TimeUtils.getDayTimeOfWeiXin(accessibilityNodeInfo.getText().toString());
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getFirstNodeAsDBLastChat(AccessibilityNodeInfo rootNode, AccessibilityNodeInfo listviewNode, int childCount) {
        if (childCount >= 1) {
            AccessibilityNodeInfo childTotalNode = listviewNode.getChild(0);
            if (childTotalNode != null) {
                List<AccessibilityNodeInfo> childContentNodes = childTotalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/im");

                if (childContentNodes != null && childContentNodes.size() > 0) {
                    List<AccessibilityNodeInfo> userNameNodes = childTotalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ik");
                    AccessibilityNodeInfo userNameNode = null;

                    if (userNameNodes != null && userNameNodes.size() > 0) {
                        userNameNode = userNameNodes.get(0);
                        if ("android.widget.ImageView".equals(userNameNode.getClassName()) && userNameNode.isClickable()) {
                            //获取聊天对象,这里两个if是为了确定找到的这个ImageView是头像的
                            if (!TextUtils.isEmpty(userNameNode.getContentDescription())) {
                                mCurrentChatName = userNameNode.getContentDescription().toString();
                                if (mCurrentChatName.contains("头像")) {
                                    mCurrentChatName = mCurrentChatName.replace("头像", "");
                                }
                            }
                        }
                    }

                    ChatBean currentChatBean = new ChatBean();
                    currentChatBean.sendContent = childContentNodes.get(0).getText().toString();
                    currentChatBean.chatBean = new UserBean();
                    currentChatBean.chatBean.weixinNickName = mCurrentChatName;
                    currentChatBean.chatBean.sessonGroupName = currentCheckGroupName;
//                    sendChatBeans.add(currentChatBean);
                    List<DBUser> dbUsers = DataApiFactory.getInstance().getIDBApi().getDBUserByNameAndgroupName(currentCheckGroupName, mCurrentChatName);
                    if (dbUsers != null && dbUsers.size() > 0) {
                        currentChatBean.chatBean.userWeiXinName = dbUsers.get(0).getWeiXinName();
                    }
                    dbLastChatBean = currentChatBean;
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean isLastTaskInCurrentPage(AccessibilityNodeInfo rootNode, AccessibilityNodeInfo listviewNode, int childCount) {
        boolean isExist = false;
        if (scrollPageCount <= MAX_SCROLL_PAGE_NUMBER) {
            scrollPageCount++;
        } else {
            //查找当前页的第一条作为dbLastChatBean
            getFirstNodeAsDBLastChat(rootNode, listviewNode, childCount);
            return true;
        }
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo childTotalNode = listviewNode.getChild(i);
            if (childTotalNode != null) {
                List<AccessibilityNodeInfo> childContentNodes = childTotalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/im");

                if (childContentNodes != null && childContentNodes.size() > 0) {
                    List<AccessibilityNodeInfo> userNameNodes = childTotalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ik");
                    AccessibilityNodeInfo userNameNode = null;

                    AccessibilityNodeInfo userInfoNode = null;
                    if (userNameNodes != null && userNameNodes.size() > 0) {
                        userNameNode = userNameNodes.get(0);
                        if ("android.widget.ImageView".equals(userNameNode.getClassName()) && userNameNode.isClickable()) {
                            //获取聊天对象,这里两个if是为了确定找到的这个ImageView是头像的
                            if (!TextUtils.isEmpty(userNameNode.getContentDescription())) {
                                mCurrentChatName = userNameNode.getContentDescription().toString();
                                if (mCurrentChatName.contains("头像")) {
                                    mCurrentChatName = mCurrentChatName.replace("头像", "");
                                }
                            }
                        }
                    }

                    ChatBean currentChatBean = new ChatBean();
                    currentChatBean.sendContent = childContentNodes.get(0).getText().toString();
                    currentChatBean.chatBean = new UserBean();
                    currentChatBean.chatBean.weixinNickName = mCurrentChatName;
                    currentChatBean.chatBean.sessonGroupName = currentCheckGroupName;
                    lastChatBean = currentChatBean;
//                    sendChatBeans.add(currentChatBean);
                    List<DBUser> dbUsers = DataApiFactory.getInstance().getIDBApi().getDBUserByNameAndgroupName(currentCheckGroupName, mCurrentChatName);
                    if (dbUsers == null || dbUsers.size() == 0) {
                        //进入验证流程
//                        List<AccessibilityNodeInfo> userInfoNodes = childTotalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/il");
//                        if (userInfoNodes != null && userInfoNodes.size() > 0) {
//                            userInfoNode = userInfoNodes.get(0);
//                        }
//
//                        if (userInfoNode != null) {
//                            Toast.makeText(this, "准备验证的用户 " + userInfoNode.getText(), Toast.LENGTH_SHORT).show();
                        try {
                            if (userNameNode != null) {
                                userNameNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                            Thread.sleep(2000);
                        } catch (Exception ex) {

                        }
//                        }
                    }

                    String lastChatContent = lastChatBean.sendContent;
                    if (dbLastChatBean != null && lastChatContent.equals(dbLastChatBean.sendContent)) {
                        List<DBUser> currentUsers = DataApiFactory.getInstance().getIDBApi().getDBUserByNameAndgroupName(currentCheckGroupName, mCurrentChatName);
                        if (currentUsers != null && currentUsers.size() > 0) {
                            DBUser dbUser = currentUsers.get(0);
                            if (dbLastChatBean.chatBean != null && !TextUtils.isEmpty(dbLastChatBean.chatBean.weixinNickName)
                                    && dbLastChatBean.chatBean.weixinNickName.equals(dbUser.getWeixinNickName())) {
                                isExist = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return isExist;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getCurrentScreenChatLogNodes(AccessibilityNodeInfo rootNode, AccessibilityNodeInfo listviewNode, int childCount) {
        //记录当前页面的结点信息，用于发送以及判断已发送结点数据
        if (sendChatBeans != null && sendChatBeans.size() == childCount) {
            return;
        }
        int i = 0;
        if (sendChatBeans == null || lastChatBean == null) {
            i = 0;
        } else {
            i = sendChatBeans.indexOf(lastChatBean);
        }

        for (; i < childCount; i++) {
            AccessibilityNodeInfo childTotalNode = listviewNode.getChild(i);
            if (childTotalNode != null) {
                List<AccessibilityNodeInfo> childContentNodes = childTotalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/im");

                if (childContentNodes != null && childContentNodes.size() > 0) {
                    List<AccessibilityNodeInfo> userNameNodes = childTotalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ik");
                    AccessibilityNodeInfo userNameNode = null;

                    AccessibilityNodeInfo userInfoNode = null;
                    if (userNameNodes != null && userNameNodes.size() > 0) {
                        userNameNode = userNameNodes.get(0);
                        if ("android.widget.ImageView".equals(userNameNode.getClassName()) && userNameNode.isClickable()) {
                            //获取聊天对象,这里两个if是为了确定找到的这个ImageView是头像的
                            if (!TextUtils.isEmpty(userNameNode.getContentDescription())) {
                                mCurrentChatName = userNameNode.getContentDescription().toString();
                                if (mCurrentChatName.contains("头像")) {
                                    mCurrentChatName = mCurrentChatName.replace("头像", "");
                                }
                            }
                        }
                    }

                    ChatBean currentChatBean = new ChatBean();
                    currentChatBean.sendContent = childContentNodes.get(0).getText().toString();
                    currentChatBean.chatBean = new UserBean();
                    currentChatBean.chatBean.weixinNickName = mCurrentChatName;
                    currentChatBean.chatBean.sessonGroupName = currentCheckGroupName;
                    lastChatBean = currentChatBean;
                    sendChatBeans.add(currentChatBean);
                    List<DBUser> dbUsers = DataApiFactory.getInstance().getIDBApi().getDBUserByNameAndgroupName(currentCheckGroupName, mCurrentChatName);
                    if (dbUsers == null || dbUsers.size() == 0) {
                        //进入验证流程
                        List<AccessibilityNodeInfo> userInfoNodes = childTotalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/il");
                        if (userInfoNodes != null && userInfoNodes.size() > 0) {
                            userInfoNode = userInfoNodes.get(0);
                        }

                        if (userInfoNode != null) {
                            Toast.makeText(this, "准备验证的用户 " + userInfoNode.getText(), Toast.LENGTH_SHORT).show();
                            try {
                                if (userNameNode != null) {
                                    userNameNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                                Thread.sleep(2000);
                            } catch (Exception ex) {

                            }
                        }
                    } else {
                        Toast.makeText(this, "准备记录内容 " + currentChatBean.sendContent, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private Method getSourceNodeIdMethod;

    private long getSourceNodeId(AccessibilityNodeInfo accessibilityNodeInfo) {
        //用于获取点击的View的id，用于检测双击操作
        if (getSourceNodeIdMethod == null) {
            Class<AccessibilityNodeInfo> eventClass = AccessibilityNodeInfo.class;
            try {
                getSourceNodeIdMethod = eventClass.getMethod("getSourceNodeId");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        if (getSourceNodeIdMethod != null) {
            try {
                return (long) getSourceNodeIdMethod.invoke(accessibilityNodeInfo);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setCurrentChatLastLog(AccessibilityNodeInfo accessibilityNodeInfo) {
        String lastChatName = "";
        String lastSendContent = "";
        List<AccessibilityNodeInfo> childContentNodes = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/im");

        if (childContentNodes != null && childContentNodes.size() > 0) {
            lastSendContent = childContentNodes.get(0).getText().toString();
            List<AccessibilityNodeInfo> userNameNodes = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ik");
            AccessibilityNodeInfo userNameNode = null;

            AccessibilityNodeInfo userInfoNode = null;
            if (userNameNodes != null && userNameNodes.size() > 0) {
                userNameNode = userNameNodes.get(0);
                if ("android.widget.ImageView".equals(userNameNode.getClassName()) && userNameNode.isClickable()) {
                    //获取聊天对象,这里两个if是为了确定找到的这个ImageView是头像的
                    if (!TextUtils.isEmpty(userNameNode.getContentDescription())) {
                        lastChatName = userNameNode.getContentDescription().toString();
                        if (lastChatName.contains("头像")) {
                            lastChatName = lastChatName.replace("头像", "");
                        }
                    }
                }
            }
            ChatBean chatBean = new ChatBean();
            chatBean.sendContent = lastSendContent;
            chatBean.chatBean = new UserBean();
            chatBean.chatBean.weixinNickName = lastChatName;
            currentLastChatBean = chatBean;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void loopCurrentChatInfo(AccessibilityNodeInfo rootNode, AccessibilityNodeInfo listviewNode, int childCount) {
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo childTotalNode = listviewNode.getChild(i);
            if (childTotalNode != null) {
                List<AccessibilityNodeInfo> childTimeNodes = childTotalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/t");
                List<AccessibilityNodeInfo> childContentNodes = childTotalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/im");

                if (childContentNodes != null && childContentNodes.size() > 0) {
                    List<AccessibilityNodeInfo> userNameNodes = childTotalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ik");
                    AccessibilityNodeInfo userNameNode = null;

                    AccessibilityNodeInfo userInfoNode = null;
                    if (userNameNodes != null && userNameNodes.size() > 0) {
                        userNameNode = userNameNodes.get(0);
                        if ("android.widget.ImageView".equals(userNameNode.getClassName()) && userNameNode.isClickable()) {
                            //获取聊天对象,这里两个if是为了确定找到的这个ImageView是头像的
                            if (!TextUtils.isEmpty(userNameNode.getContentDescription())) {
                                mCurrentChatName = userNameNode.getContentDescription().toString();
                                if (mCurrentChatName.contains("头像")) {
                                    mCurrentChatName = mCurrentChatName.replace("头像", "");
                                }
                            }
                        }
                    }

                    ChatBean currentChatBean = new ChatBean();
                    currentChatBean.sendContent = childContentNodes.get(0).getText().toString();
                    currentChatBean.chatBean = new UserBean();
                    currentChatBean.chatBean.weixinNickName = mCurrentChatName;
                    currentChatBean.chatBean.sessonGroupName = currentCheckGroupName;
                    if (childTimeNodes != null && childTimeNodes.size() > 0) {
                        currentLastChatLogTime = TimeUtils.getDayTimeOfWeiXin(childTimeNodes.get(0).getText().toString());
                    } else {
                        if (currentLastChatLogTime != 0) {
                            currentLastChatLogTime = currentLastChatLogTime + 1000;
                        }
                    }
                    currentChatBean.sendTime = String.valueOf(currentLastChatLogTime);
                    lastChatBean = currentChatBean;
                    sendChatBeans.add(currentChatBean);

                    List<DBUser> dbUsers = DataApiFactory.getInstance().getIDBApi().getDBUserByNameAndgroupName(currentCheckGroupName, mCurrentChatName);
                    if (dbUsers != null && dbUsers.size() > 0) {
                        currentChatBean.chatBean.userWeiXinName = dbUsers.get(0).getWeiXinName();
                    }

                    String lastChatContent = lastChatBean.sendContent;
                    if (currentLastChatBean != null && lastChatContent.equals(currentLastChatBean.sendContent)) {
                        if (dbUsers != null && dbUsers.size() > 0) {
                            DBUser dbUser = dbUsers.get(0);
                            if (currentLastChatBean.chatBean != null && !TextUtils.isEmpty(currentLastChatBean.chatBean.weixinNickName)
                                    && currentLastChatBean.chatBean.weixinNickName.equals(dbUser.getWeixinNickName())) {
                                isChatLogFinished = true;
                                return;
                            }
                        }
                    }
                }
            }
        }

        //结束遍历条件: 1. 查找到最后一条记录，在for循环当中直接返回 2.遍历失败，遍历到最终一条，即scroll到底部不能继续滚动
        List<AccessibilityNodeInfo> chatInfoNodes = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/p");
        if (chatInfoNodes != null) {
            int chatInfoNodeCount = chatInfoNodes.size();
            if (chatInfoNodeCount > 0) {
                if (currentChatPageBottomNode == null) {
                    //第一次遍历到底部
                    currentChatPageBottomNode = chatInfoNodes.get(chatInfoNodeCount - 1);
                } else {
                    long rsourseId = getSourceNodeId(currentChatPageBottomNode);
                    long chatInfoFirstNodeSourseId = getSourceNodeId(chatInfoNodes.get(chatInfoNodeCount - 1));
                    if (rsourseId == chatInfoFirstNodeSourseId) {
                        //已经到达底部，则返回进行消息sendChatBeans List的上传
                        isChatLogFinished = true;
                        return;
                    } else {
                        currentChatPageBottomNode = chatInfoNodes.get(chatInfoNodeCount - 1);
                    }
                }
            }
        }
        if ("android.widget.ListView".equals(listviewNode.getClassName()) && listviewNode.isScrollable()) {
            isScrollFromUser = true;
            listviewNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getChatInfo(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            List<AccessibilityNodeInfo> chatListNodes = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a3e");
            List<AccessibilityNodeInfo> chatInfoNodes = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/p");
            if (chatInfoNodes != null && chatInfoNodes.size() > 0) {
                if (currentChatPageTopNode == null) {
                    //第一次进入本聊天界面
                    currentChatPageTopNode = chatInfoNodes.get(0);
                    setCurrentChatLastLog(chatInfoNodes.get(chatInfoNodes.size() - 1));
                } else {
                    long rsourseId = getSourceNodeId(currentChatPageTopNode);
                    long chatInfoFirstNodeSourseId = getSourceNodeId(chatInfoNodes.get(0));
                    if (rsourseId == chatInfoFirstNodeSourseId && !isScrollFromUser && currentActionType != ACTION_TYPE_SEND_RECENT_CHAT_LOG) {
                        return;
                    } else {
                        currentChatPageTopNode = chatInfoNodes.get(0);
                    }
                }
                isScrollFromUser = false;
            }
            if (chatListNodes != null && chatListNodes.size() > 0) {
                AccessibilityNodeInfo listviewNode = chatListNodes.get(0);
                int childCount = listviewNode.getChildCount();
//                Toast.makeText(this, "需要上传的消息数为 " + childCount, Toast.LENGTH_SHORT).show();

                //查找当前group的最近一条消息记录
                DBGroupLastChatLog dbGroupLastChatLog = null;
                switch (currentActionType) {
                    case ACTION_TYPE_UN_START: {
                        //第一次进入聊天详情页，查找最后一次聊天记录
                        dbGroupLastChatLog = DataApiFactory.getInstance().getIDBApi().getGroupLastChatLog(currentCheckGroupName);
                        if (dbGroupLastChatLog != null) {
                            ChatBean currentChatBean = new ChatBean();
                            currentChatBean.sendContent = dbGroupLastChatLog.getSendContent();
                            currentChatBean.chatBean = new UserBean();
                            currentChatBean.chatBean.weixinNickName = dbGroupLastChatLog.getSendWeiXinNickName();
                            currentChatBean.chatBean.userWeiXinName = dbGroupLastChatLog.getSendWeiXinName();
                            currentChatBean.chatBean.sessonGroupName = dbGroupLastChatLog.getSessonGroupName();
                            currentChatBean.sendTime = dbGroupLastChatLog.getSendTime();
                            currentLastChatLogTime = Long.valueOf(dbGroupLastChatLog.getSendTime());
                            dbLastChatBean = currentChatBean;
                            boolean isExistInCurrentPage = isLastTaskInCurrentPage(rootNode, listviewNode, childCount);
                            if (isExistInCurrentPage) {
                                //最近一条消息在本界面当中
                                if (currentLastChatLogTime != 0) {
                                    currentActionType = ACTION_TYPE_SEND_RECENT_CHAT_LOG;
                                } else {
                                    currentActionType = ACTION_TYPE_GET_LAST_CHAT_LOG_TIME;
                                }
                            } else {
                                //向上滚动一页
                                if ("android.widget.ListView".equals(listviewNode.getClassName()) && listviewNode.isScrollable()) {
                                    listviewNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                                    currentActionType = ACTION_TYPE_FIND_LAST_CHAT_LOG;
                                }
                            }
                        } else {
                            //循环向上进行查找
                            isLastTaskInCurrentPage(rootNode, listviewNode, childCount);
                            if ("android.widget.ListView".equals(listviewNode.getClassName()) && listviewNode.isScrollable()) {
                                listviewNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                                currentActionType = ACTION_TYPE_FIND_LAST_CHAT_LOG;
                            }
                        }
                    }
                    break;
                    case ACTION_TYPE_FIND_LAST_CHAT_LOG: {
                        //查找消息所在的页面
                        boolean isExistInCurrentPage = isLastTaskInCurrentPage(rootNode, listviewNode, childCount);
                        if (isExistInCurrentPage) {
                            //最近一条消息在本界面当中，判断是否因为超出滑动次数，并开始进行最近一次时间的遍历
                            if (scrollPageCount > MAX_SCROLL_PAGE_NUMBER) {
                                getFirstNodeAsDBLastChat(rootNode, listviewNode, childCount);
                            }
                            if (currentLastChatLogTime != 0) {
                                currentActionType = ACTION_TYPE_SEND_RECENT_CHAT_LOG;
                                isScrollFromUser = true;
                                getChatInfo(rootNode);
                            } else {
                                currentActionType = ACTION_TYPE_GET_LAST_CHAT_LOG_TIME;
                                boolean isGetChatTime = findDBLastChatLogTime(rootNode, listviewNode, childCount);
                                if (isGetChatTime) {
                                    currentActionType = ACTION_TYPE_SEND_RECENT_CHAT_LOG;
                                    isScrollFromUser = true;
                                    getChatInfo(rootNode);
                                } else {
                                    //继续向上滚动，并查找有效时间
                                    if ("android.widget.ListView".equals(listviewNode.getClassName()) && listviewNode.isScrollable()) {
                                        listviewNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                                        currentActionType = ACTION_TYPE_GET_LAST_CHAT_LOG_TIME;
                                    }
                                }
                            }
                        } else {
                            //向上滚动一页
                            if ("android.widget.ListView".equals(listviewNode.getClassName()) && listviewNode.isScrollable()) {
                                listviewNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                                currentActionType = ACTION_TYPE_FIND_LAST_CHAT_LOG;
                            }
                        }
                    }
                    break;
                    case ACTION_TYPE_SEND_RECENT_CHAT_LOG: {
                        //开始遍历并上传消息记录，如果没有在上一页就从本页面开始进行上传并依次向下循环，直至本次的最终消息，上传完毕后，保存最后一次的消息记录至数据库当中
                        loopCurrentChatInfo(rootNode, listviewNode, childCount);
                    }
                    break;
                    case ACTION_TYPE_GET_LAST_CHAT_LOG_TIME: {
                        //如果数据库中保存的上次聊天时间为空，则向上翻滚，查找到最近一条展示的时间作为聊天时间进行记录
                        boolean isExistInCurrentPage = findDBLastChatLogTime(rootNode, listviewNode, childCount);
                        if (isExistInCurrentPage) {
                            currentActionType = ACTION_TYPE_SEND_RECENT_CHAT_LOG;
                            isScrollFromUser = true;
                            getChatInfo(rootNode);
                        } else {
                            if ("android.widget.ListView".equals(listviewNode.getClassName()) && listviewNode.isScrollable()) {
                                listviewNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                                currentActionType = ACTION_TYPE_GET_LAST_CHAT_LOG_TIME;
                            }
                        }
                    }
                    break;
                }

//                Toast.makeText(this, "本次遍历任务 " + currentActionType, Toast.LENGTH_SHORT).show();

                if (isChatLogFinished && !isCurrentChatLogSend) {
                    try {
                        Thread.sleep(3000);
                        if (sendChatBeans != null && sendChatBeans.size() > 0) {

                            dbGroupLastChatLog = DataApiFactory.getInstance().getIDBApi().getGroupLastChatLog(currentCheckGroupName);
                            int i = 0;
                            if (dbGroupLastChatLog != null) {
                                for (; i < sendChatBeans.size(); i++) {
                                    String sendContent = sendChatBeans.get(i).sendContent;
                                    String sendNickName = sendChatBeans.get(i).chatBean.weixinNickName;
                                    if (sendContent.equals(dbGroupLastChatLog.getSendContent()) && sendNickName.equals(dbGroupLastChatLog.getSendWeiXinNickName())) {
                                        break;
                                    }
                                }
                            }
                            List<ChatBean> realSendChatBeans = new ArrayList<>();
                            int startIndex = i;
                            if (startIndex + 1 < sendChatBeans.size()) {
                                startIndex = i + 1;
                            }
                            realSendChatBeans.addAll(sendChatBeans.subList(startIndex, sendChatBeans.size()));
                            if (TextUtils.isEmpty(currentLastChatBean.sendTime)) {
                                currentLastChatBean.sendTime = sendChatBeans.get(sendChatBeans.size() - 1).sendTime;
                            }
                            for (int j = 0; j < realSendChatBeans.size(); j++) {
                                ChatBean chatBean = realSendChatBeans.get(j);
                                if (!TextUtils.isEmpty(chatBean.sendTime)) {
                                    chatBean.sendTime = TimeUtils.convertTimeToUploadTimeByLongString(chatBean.sendTime);
                                }
                            }
                            Toast.makeText(this, "本次上传数据为 " + realSendChatBeans.toString(), Toast.LENGTH_SHORT).show();
                            Log.i(TAG, realSendChatBeans.toString());
                            uploadChatMessage(realSendChatBeans);
                            sendChatBeans.clear();
                        }
                    } catch (Exception ex) {
                        Log.i(TAG, ex.toString());
                    }
                    saveThisTimeLastChatLog();

                    scrollPageCount = 0;
                    currentActionType = -1;
                    currentLastChatLogTime = 0;
                    currentChatPageTopNode = null;
                    isCurrentChatLogSend = true;
                    isScrollFromUser = true;
                    currentChatBean = null;
                    List<AccessibilityNodeInfo> backNodeInfo = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gn");
                    backNodeInfo.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

    private void saveThisTimeLastChatLog() {
        String groupName = currentCheckGroupName;
//        currentLastChatBean.
        if (currentLastChatBean == null) {
            return;
        }
        Log.i(TAG, "save last chat bean is " + currentLastChatBean.sendContent);
        DataApiFactory.getInstance().getIDBApi().saveGroupLastChatLog(groupName, String.valueOf(currentLastChatLogTime),
                currentLastChatBean.chatBean.userWeiXinName, currentLastChatBean.chatBean.weixinNickName, currentLastChatBean.sendContent, 0);
    }

    private void uploadChatMessage(List<ChatBean> chatBeens) {
        //文件中读取出List<UploadChatBean>，若为NULL，则说明不存在上传失败的情况，直接上传本次数据即可
        String md5GroupId = QEncodeUtil.GetMD5Code(currentCheckGroupName);
        String currentGroupUpoadFilePath = PathUtils.getAppUploadLogPath() + File.separator + md5GroupId + ".txt";
        List<UploadChatBean> uploadChatBeens = new ArrayList<>();
        UploadChatBean uploadChatBean = new UploadChatBean();
        uploadChatBean.sendContent = chatBeens;
        uploadChatBean.sendChatContentCount = chatBeens.size();
        uploadChatBean.sendTime = chatBeens.get(0).sendTime;
        uploadChatBean.sessonGroupName = currentCheckGroupName;
        uploadChatBean.sessonGroupId = md5GroupId;
        File file = new File(currentGroupUpoadFilePath);
        if (file.exists()) {
            try {
                ObjectInputStream is = new ObjectInputStream(new FileInputStream(currentGroupUpoadFilePath));
                uploadChatBeens = (List<UploadChatBean>) is.readObject();
                is.close();
                uploadChatBeens.add(uploadChatBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
            file.delete();
        } else {
            uploadChatBeens.add(uploadChatBean);
        }

//        uploadChatBean(uploadChatBeens, currentGroupUpoadFilePath);
    }

    private void writeUploadBeansToFile(List<UploadChatBean> uploadChatBeens, String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            ObjectOutputStream outWrite = new ObjectOutputStream(new FileOutputStream(filePath));
            outWrite.writeObject(uploadChatBeens);
            outWrite.close();
        } catch (Exception ex) {

        }
    }

    /**
     * 遍历所有控件:这里分四种情况
     * 文字聊天: 一个TextView，并且他的父布局是android.widget.LinearLayout
     * 语音的秒数: 一个TextView，并且他的父布局是android.widget.RelativeLayout，但是他的格式是0"的格式，所以可以通过这个来区分
     * 图片:一个ImageView,并且他的父布局是android.widget.FrameLayout,描述中包含“图片”字样（发过去的图片），发回来的图片现在还无法监听
     * 表情:也是一个ImageView,并且他的父布局是android.widget.LinearLayout
     * 小视频的秒数:一个TextView，并且他的父布局是android.widget.FrameLayout，但是他的格式是00:00"的格式，所以可以通过这个来区分
     *
     * @param node
     */
    public void GetChatRecord(AccessibilityNodeInfo node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo nodeChild = node.getChild(i);

            //聊天内容是:文字聊天(包含语音秒数)
            if ("android.widget.TextView".equals(nodeChild.getClassName()) && "android.widget.LinearLayout".equals(nodeChild.getParent().getClassName().toString())) {
                if (!TextUtils.isEmpty(nodeChild.getText())) {
                    String RecordText = nodeChild.getText().toString();
                    //这里加个if是为了防止多次触发TYPE_VIEW_SCROLLED而打印重复的信息
                    if (!RecordText.equals(ChatRecord)) {
                        ChatRecord = RecordText;
                        //判断是语音秒数还是正常的文字聊天,语音的话秒数格式为5"
                        if (ChatRecord.contains("\"")) {
                            Toast.makeText(this, mCurrentChatName + "发了一条" + ChatRecord + "的语音", Toast.LENGTH_SHORT).show();

                            Log.e("WeChatLog", mCurrentChatName + "发了一条" + ChatRecord + "的语音");
                        } else {
                            //这里在加多一层过滤条件，确保得到的是聊天信息，因为有可能是其他TextView的干扰，例如名片等
                            if (nodeChild.isLongClickable()) {
                                Toast.makeText(this, mCurrentChatName + "：" + ChatRecord, Toast.LENGTH_SHORT).show();

                                Log.e("WeChatLog", mCurrentChatName + "：" + ChatRecord);
                            }

                        }
                        return;
                    }
                }
            }

            //聊天内容是:表情
            if ("android.widget.ImageView".equals(nodeChild.getClassName()) && "android.widget.LinearLayout".equals(nodeChild.getParent().getClassName().toString())) {
                Toast.makeText(this, mCurrentChatName + "发的是表情", Toast.LENGTH_SHORT).show();

                Log.e("WeChatLog", mCurrentChatName + "发的是表情");

                return;
            }

            //聊天内容是:图片
            if ("android.widget.ImageView".equals(nodeChild.getClassName())) {
                //安装软件的这一方发的图片（另一方发的暂时没实现）
                if ("android.widget.FrameLayout".equals(nodeChild.getParent().getClassName().toString())) {
                    if (!TextUtils.isEmpty(nodeChild.getContentDescription())) {
                        if (nodeChild.getContentDescription().toString().contains("图片")) {
                            Toast.makeText(this, mCurrentChatName + "发的是图片", Toast.LENGTH_SHORT).show();

                            Log.e("WeChatLog", mCurrentChatName + "发的是图片");
                        }
                    }
                }
            }

//            //聊天内容是:小视频秒数,格式为00：00
//            if ("android.widget.TextView".equals(nodeChild.getClassName()) && "android.widget.FrameLayout".equals(nodeChild.getParent().getClassName().toString())) {
//                if (!TextUtils.isEmpty(nodeChild.getText())) {
//                    String second = nodeChild.getText().toString().replace(":", "");
//                    //正则表达式，确定是不是纯数字,并且做重复判断
//                    if (second.matches("[0-9]+") && !second.equals(VideoSecond)) {
//                        VideoSecond = second;
//                        Toast.makeText(this, ChatName + "发了一段" + nodeChild.getText().toString() + "的小视频", Toast.LENGTH_SHORT).show();
//
//                        Log.e("WeChatLog", "发了一段" + nodeChild.getText().toString() + "的小视频");
//                    }
//                }
//
//            }

            GetChatRecord(nodeChild);
        }
    }

}
