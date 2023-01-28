package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;

    private int messageId=0;
    private static int groupCount;
    private static int userCount;
    //private HashMap<String,String> userMap;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.groupCount = 0;
        this.messageId = 0;
        this.userCount=0;
        //this.userMap=new HashMap<>();
    }

    public String createUser(String name, String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
        else {
            userMobile.add(mobile);
            User user = new User(name, mobile);
            userCount+=1;
            return "New user created";
        }
    }

    public Group createGroup(List<User> users){
        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.

        //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
        //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.
        if(users.size()<2)
            return null;
        User admin=users.get(0);

        if(!userMobile.contains(admin.getMobile()))
            return null;

        for(int i=1; i<users.size();i++){
            User user=users.get(i);
            if(!userMobile.contains(user.getMobile()))
                return null;
        }

        String gName;
        if(users.size()==2){
            gName=users.get(1).getName();
        }
        else
            gName="Group "+ ++groupCount;
        Group group=new Group(gName,users.size());
        adminMap.put(group,admin);
        groupUserMap.put(group,users);
        return group;
    }

    public int createMessage(String content){
        int id=messageId+1;
        Message message=new Message(id,content);
        return ++messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        if(!groupUserMap.containsKey(group))
            throw new Exception("You are not allowed to send message");

        List<User> users=groupUserMap.get(group);
        if(!users.contains(sender))
            throw new Exception("You are not allowed to send message");

        senderMap.put(message,sender);

        List<Message> msgList=groupMessageMap.getOrDefault(group,new ArrayList<>());
        msgList.add(message);

        groupMessageMap.put(group,msgList);
        return msgList.size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.
        if(!groupUserMap.containsKey(group))
            throw new Exception("Group does not exist");
        if(!adminMap.get(group).equals(approver))
            throw new Exception("Approver does not have rights");
        List<User> ulist=groupUserMap.get(group);
        if(!ulist.contains(user))
            throw new Exception("User is not a participant");

        Collections.swap(ulist,0,ulist.indexOf(user));
        groupUserMap.put(group,ulist);

        adminMap.remove(group);
        adminMap.put(group,user);
        return "success";
    }



}
