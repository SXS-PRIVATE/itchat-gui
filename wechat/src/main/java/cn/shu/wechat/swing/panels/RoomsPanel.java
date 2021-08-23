package cn.shu.wechat.swing.panels;

import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.adapter.RoomItemViewHolder;
import cn.shu.wechat.swing.adapter.RoomItemsAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCListView;
import cn.shu.wechat.swing.db.model.Room;
import cn.shu.wechat.swing.entity.RoomItem;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.utils.ExecutorServiceUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 左侧聊天列表
 * Created by 舒新胜 on 17-5-30.
 */
public class RoomsPanel extends ParentAvailablePanel {
    private static RoomsPanel context;


    /**
     * 未读消息总数
     */
    private static final AtomicInteger UNREAD_TOTAL_COUNT = new AtomicInteger(0);
    /**
     * 聊天列表视图数据
     */
    private RCListView roomItemsListView;

    /**
     * 当前聊天列表
     */
    private final List<RoomItem> roomItemList = new ArrayList<>();


    public RoomsPanel(JPanel parent) {
        super(parent);
        context = this;

        initComponents();
        initView();
        initData();
        roomItemsListView.setAdapter(new RoomItemsAdapter(roomItemList));
    }


    /**
     * 消息已读数量
     * @param count 本次已读
     */
    public static void updateUnreadTotalCount(int count){
        if (count != 0){
            int i = UNREAD_TOTAL_COUNT.addAndGet(count);
        }
        if (UNREAD_TOTAL_COUNT.get() == 0){
            MainFrame.getContext().setTrayFlashing(false);
        }
    }

    private void initComponents() {
        roomItemsListView = new RCListView();
        roomItemsListView.getVerticalScrollBar().setUnitIncrement(RoomItemViewHolder.HEIGHT/3);
    }

    private void initView() {
        setLayout(new GridBagLayout());
        roomItemsListView.setContentPanelBackground(Colors.DARK);
        add(roomItemsListView, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1));
        //add(scrollPane, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1));
    }

    private void initData() {
        roomItemList.clear();
        //从核心类加载房间列表
/*        Set<Contacts> recentContacts = Core.getRecentContacts();
        for (Contacts recentContact : recentContacts) {
            RoomItem item = new RoomItem();
            item.setRoomId(recentContact.getUsername());
            item.setTimestamp(System.currentTimeMillis());
            item.setName(ContactsTools.getContactDisplayNameByUserName(recentContact.getUsername()));
            item.setLastMessage("");
            item.setUnreadCount(0);
            item.setGroup(recentContact.getUsername().startsWith("@@"));
            item.setHeadImgPath(recentContact.getHeadimgurl());
            item.setRefreshHead(true);
            roomItemList.add(item);
        }*/
    }
    /**
     * 进入房间
     *
     * @param roomId 房间id
     */
    public void enterRoom(String roomId) {
        //切换显示层
        RoomChatPanelCard roomChatPanelCard = RoomChatContainer.getContext().createAndShow(roomId);
        RoomChatContainer.getContext().show(roomId);
        //显示聊天界面
        roomChatPanelCard.showPanel(RoomChatPanelCard.MESSAGE);
        //更新聊天列表未读数量
         updateUnreadCount(roomId,0);
        //发送消息已读通知
        ExecutorServiceUtil.getGlobalExecutorService().execute(() -> MessageTools.sendStatusNotify(roomId));
    }
    /**
     * 添加房间
     *
     * @param roomId 联系人
     * @param latestMsg     最新消息
     */
    private void addRoom(String roomId, String latestMsg, int msgCount) {
        Contacts contacts = Core.getMemberMap().get(roomId);
        addRoom(new RoomItem(contacts, latestMsg, msgCount));
    }
    /**
     * 添加房间
     *
     * @param roomId 联系人
     */
    public void addRoom(String roomId) {
        addRoom(roomId, "", 0);
    }
    /**
     * 添加房间
     * @param item
     */
    private void addRoom(RoomItem item) {
        roomItemList.add(0, item);
        roomItemsListView.notifyDataSetChanged(false);
        roomItemsListView.scrollToPosition(0);
    }

    /**
     * 添加房间
     *
     * @param roomId 联系人
     * @param latestMsg  最新消息
     */
    public void addRoomOrOpenRoomNotSwitch(String roomId, String latestMsg, int msgCount) {

        //更新聊天列表
        Set<String> recentContacts = Core.getRecentContacts();
        if (!recentContacts.contains(roomId)) {
            //添加新房间并制定
            addRoom(roomId, latestMsg, msgCount);
            recentContacts.add(roomId);
        } else {
            //更新消息 置顶
           updateRoomItem(roomId, msgCount, latestMsg, System.currentTimeMillis());
        }
    }



    /**
     * 添加房间
     *
     * @param roomId 联系人id
     * @param latestMsg  最新消息
     */
    public void addRoomOrOpenRoom(String roomId, String latestMsg, int msgCount) {
        addRoomOrOpenRoomNotSwitch(roomId,latestMsg,msgCount);
        TabOperationPanel.getContext().switchToChatLabel();
    }
    /**
     * 批量添加房间
     * @param items
     */
    public void addRoom(List<RoomItem> items) {
        roomItemList.addAll(items);
        roomItemsListView.notifyDataSetChanged(false);
    }

    /**
     * 重绘整个列表
     */
    public void notifyDataSetChanged(boolean keepSize) {
        initData();
        roomItemsListView.notifyDataSetChanged(keepSize);
    }

    /**
     * 添加
     */
    /**
     * 指定位置添加元素
     * @param pos 位置
     */
    public void notifyItemInserted(int pos) {
        roomItemsListView.notifyItemInserted(pos,false);
    }

    /**
     * 更新房间列表
     * 当这条消息所在的房间在当前房间列表中排在第一位时，此时房间列表项目顺序不变，无需重新排列
     * 因此无需更新整个房间列表，只需更新第一个项目即可
     *
     * @param msgRoomId 房间ID
     */
    public void updateRoomsList(String msgRoomId) {
        String roomId = (String) ((RoomItemViewHolder) (roomItemsListView.getItem(0))).getTag();
        if (roomId.equals(msgRoomId)) {
            Room room = null;//roomService.findById(roomId);
            for (RoomItem roomItem : roomItemList) {
                if (roomItem.getRoomId().equals(roomId)) {
                    roomItem.setUnreadCount(100);
                    //roomItem.setTimestamp(room.getLastChatAt());
                    // roomItem.setLastMessage(room.getLastMessage());
                    break;
                }
            }

            roomItemsListView.notifyItemChanged(0);
        } else {
            notifyDataSetChanged(false);
        }
    }

    /**
     * 更新房间未读消息数
     *
     * @param roomId      房间id
     * @param unReadCount 消息数
     */
    public void updateUnreadCount(String roomId, int unReadCount) {
        for (int i = 0; i < roomItemList.size(); i++) {
            RoomItem item = roomItemList.get(i);
            if (item.getRoomId().equals(roomId)) {
                //找到对应房间
                if (unReadCount == 0) {
                    updateUnreadTotalCount(-item.getUnreadCount());
                    item.setUnreadCount(0);
                } else if (unReadCount != -1) {
                    item.setUnreadCount(item.getUnreadCount() + unReadCount);
                }
                roomItemsListView.notifyItemChanged(i);
                break;
            }
        }
    }



    /**
     * 更新指定房间项目
     * @param roomId 房间id
     * @param unReadCount 未读消息数
     * @param lastMsg 最近的一条消息
     * @param time 时间
     */
    public void updateRoomItem(String roomId, int unReadCount, String lastMsg, Long time) {
        if (roomId == null || roomId.isEmpty()) {
            notifyDataSetChanged(true);
            return;
        }
        //updateRoomsList(roomId);

        for (int i = 0; i < roomItemList.size(); i++) {
            RoomItem item = roomItemList.get(i);
            if (item.getRoomId().equals(roomId)) {
                //找到对应房间

                if (lastMsg != null) {
                    item.setLastMessage(lastMsg);
                }
                if (time != null) {
                    item.setTimestamp(time);
                }
                if (roomId.equals(RoomChatContainer.getCurrRoomId())) {
                    //当前显示的房间和新消息房间一样，则不需要在房间条目上显示未读消息数量
                    item.setUnreadCount(0);
                } else if (unReadCount != -1) {
                    item.setUnreadCount(item.getUnreadCount() + unReadCount);
                }
                //最新消息移到首行
                if (i != 0) {
                    roomItemList.add(0, roomItemList.remove(i));
                    //重绘整个列表
                    roomItemsListView.notifyDataSetChanged(true);
                  /*  //假如本来再2位置的移到0位置  那么只需重绘前三个View
                    roomItemsListView.notifyItemRangeInserted(0,i+1);*/
                } else {
                    //当前消息位于首行，则无需重绘整个列表
                    roomItemsListView.notifyItemChanged(0);
                }
                return;
            }
        }

    }

    /**
     * 激活指定的房间项目
     *
     * @param position
     */
    public void activeItem(int position) {
        RoomItemViewHolder holder = (RoomItemViewHolder) roomItemsListView.getItem(position);
        setItemBackground(holder, Colors.ITEM_SELECTED);
        RoomItemsAdapter adapter = (RoomItemsAdapter) (roomItemsListView.getAdapter());
        adapter.setSelectedViewHolder(holder);
        for (int i = 0; i < roomItemsListView.getItems().size(); i++) {
            if (i == position) {
                continue;
            }
            holder = (RoomItemViewHolder) roomItemsListView.getItem(i);
            setItemBackground(holder, Colors.DARK);
        }
        scrollToPosition(position*RoomItemViewHolder.HEIGHT);
    }

    /**
     * 激活指定的房间项目
     *
     * @param name
     */
    public void activeItem(String name) {
        for (int i = 0; i < roomItemList.size(); i++) {
            RoomItem roomItem = roomItemList.get(i);
            if (roomItem.getRoomId().equals(name)) {
                activeItem(i);
                return;
            }
        }

    }


    /**
     * 设置每个房间项目的背影色
     *
     * @param holder
     * @param color
     */
    private void setItemBackground(RoomItemViewHolder holder, Color color) {
        holder.setBackground(color);
        holder.nameBrief.setBackground(color);
        holder.timeUnread.setBackground(color);
    }

    public void scrollPoint(int point){
        roomItemsListView.getVerticalScrollBar().setValue(point);
    }

    public void scrollToPosition(int point){
        roomItemsListView.scrollToPosition(point);
    }

    public static RoomsPanel getContext() {
        return context;
    }

}
