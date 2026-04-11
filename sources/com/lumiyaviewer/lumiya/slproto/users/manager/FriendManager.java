package com.lumiyaviewer.lumiya.slproto.users.manager;

import com.google.common.collect.ImmutableList;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.dao.DaoSession;
import com.lumiyaviewer.lumiya.dao.Friend;
import com.lumiyaviewer.lumiya.dao.FriendDao;
import com.lumiyaviewer.lumiya.react.RequestFinalProcessor;
import com.lumiyaviewer.lumiya.react.Subscribable;
import com.lumiyaviewer.lumiya.react.SubscriptionPool;
import com.lumiyaviewer.lumiya.slproto.auth.SLAuthReply;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class FriendManager {

    @Nonnull
    private final ChatterList chatterList;

    @Nonnull
    private final FriendDao friendDao;

    @Nonnull
    private final UserManager userManager;
    private final SubscriptionPool<UUID, Boolean> onlineStatus = new SubscriptionPool<>();
    private final OnListUpdated onFriendListUpdated = new OnListUpdated() { // from class: com.lumiyaviewer.lumiya.slproto.users.manager.FriendManager.1
        @Override // com.lumiyaviewer.lumiya.slproto.users.manager.OnListUpdated
        public void onListUpdated() {
            FriendManager.this.chatterList.notifyListUpdated(ChatterListType.Friends);
        }
    };
    private final OnListUpdated onFriendsOnlineListUpdated = new OnListUpdated() { // from class: com.lumiyaviewer.lumiya.slproto.users.manager.FriendManager.2
        @Override // com.lumiyaviewer.lumiya.slproto.users.manager.OnListUpdated
        public void onListUpdated() {
            FriendManager.this.chatterList.notifyListUpdated(ChatterListType.FriendsOnline);
        }
    };

    public FriendManager(@Nonnull UserManager userManager, @Nonnull DaoSession daoSession, @Nonnull ChatterList chatterList) {
        this.userManager = userManager;
        this.friendDao = daoSession.getFriendDao();
        this.chatterList = chatterList;
        new RequestFinalProcessor<UUID, Boolean>(this.onlineStatus, userManager.getDatabaseExecutor()) { // from class: com.lumiyaviewer.lumiya.slproto.users.manager.FriendManager.3
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // com.lumiyaviewer.lumiya.react.RequestFinalProcessor
            public Boolean processRequest(@Nonnull UUID uuid) {
                Friend friendLoad = FriendManager.this.friendDao.load(uuid);
                if (friendLoad != null) {
                    return Boolean.valueOf(friendLoad.getIsOnline());
                }
                return false;
            }
        };
    }

    public void addFriend(UUID uuid) {
        if (this.friendDao.load(uuid) == null) {
            this.friendDao.insert(new Friend(uuid, 1, 1, false));
        }
        this.chatterList.updateList(ChatterListType.Friends);
        this.chatterList.updateList(ChatterListType.FriendsOnline);
    }

    public Friend getFriend(@Nullable UUID uuid) {
        if (uuid != null) {
            return this.friendDao.load(uuid);
        }
        return null;
    }

    public ChatterDisplayDataList getFriendList() {
        return new FriendDisplayDataList(this.userManager, this.onFriendListUpdated, false);
    }

    public ChatterDisplayDataList getFriendsOnlineList() {
        return new FriendDisplayDataList(this.userManager, this.onFriendsOnlineListUpdated, true);
    }

    public Subscribable<UUID, Boolean> getOnlineStatus() {
        return this.onlineStatus;
    }

    public void removeFriend(UUID uuid) {
        this.friendDao.deleteByKey(uuid);
        this.chatterList.updateList(ChatterListType.Friends);
        this.chatterList.updateList(ChatterListType.FriendsOnline);
    }

    public void setUsersOnline(List<UUID> list, boolean z) {
        for (UUID uuid : list) {
            Friend friendLoad = this.friendDao.load(uuid);
            if (friendLoad != null) {
                friendLoad.setIsOnline(z);
                this.friendDao.update(friendLoad);
            }
            this.onlineStatus.requestUpdate(uuid);
        }
        this.chatterList.updateList(ChatterListType.FriendsOnline);
    }

    public void updateFriendList(ImmutableList<SLAuthReply.Friend> immutableList) {
        HashSet hashSet = new HashSet();
        for (SLAuthReply.Friend friend : immutableList) {
            UUID uuid = friend.uuid;
            Friend friendLoad = this.friendDao.load(uuid);
            if (friendLoad == null) {
                this.friendDao.insertOrReplace(new Friend(uuid, friend.rightsGiven, friend.rightsHas, false));
            } else if (friendLoad.getRightsGiven() != friend.rightsGiven || friendLoad.getRightsHas() != friend.rightsHas || friendLoad.getIsOnline()) {
                friendLoad.setRightsGiven(friend.rightsGiven);
                friendLoad.setRightsHas(friend.rightsHas);
                friendLoad.setIsOnline(false);
                this.friendDao.update(friendLoad);
            }
            hashSet.add(uuid);
        }
        List<Friend> listLoadAll = this.friendDao.loadAll();
        Debug.Printf("FriendList: update[1], got %d friends", Integer.valueOf(listLoadAll.size()));
        for (Friend friend2 : listLoadAll) {
            if (!hashSet.contains(friend2.getUuid())) {
                this.friendDao.delete(friend2);
            }
        }
        this.chatterList.updateList(ChatterListType.Friends);
        this.chatterList.updateList(ChatterListType.FriendsOnline);
    }
}
