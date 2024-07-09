package com.myproject.locket_clone.ui.friends

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.myproject.locket_clone.R
import com.myproject.locket_clone.databinding.ActivityFriendsBinding
import com.myproject.locket_clone.model.Friend
import com.myproject.locket_clone.model.Fullname
import com.myproject.locket_clone.model.Home
import com.myproject.locket_clone.model.UserProfile
import com.myproject.locket_clone.recycler_view.FriendRequestsAdapter
import com.myproject.locket_clone.recycler_view.FriendRequestsInterface
import com.myproject.locket_clone.recycler_view.FriendsListAdapter
import com.myproject.locket_clone.recycler_view.FriendsListInterface
import com.myproject.locket_clone.recycler_view.SearchUserAdapter
import com.myproject.locket_clone.recycler_view.SearchUserInterface
import com.myproject.locket_clone.repository.Repository
import com.myproject.locket_clone.ui.home.HomeActivity
import com.myproject.locket_clone.viewmodel.home.HomeViewModel
import com.myproject.locket_clone.viewmodel.home.HomeViewModelFactory

class FriendsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFriendsBinding
    private lateinit var friendsListadapter: FriendsListAdapter
    private lateinit var friendRequestsadapter: FriendRequestsAdapter
    private  var friendList = ArrayList<Friend>()
    private  var receivedInviteList = ArrayList<Friend>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Khoi tao ban dau
        val repository by lazy { Repository() }
        val viewModelFactory by lazy { HomeViewModelFactory(repository) }
        val homeViewModel = ViewModelProvider(this, viewModelFactory).get(
            HomeViewModel::class.java)

        //Nhan du lieu tu HomeActivity
        val userProfile: UserProfile? = intent.getSerializableExtra("USER_PROFILE") as? UserProfile
        friendList = (intent.getSerializableExtra("FRIEND_LIST") as ArrayList<Friend>?)!!
        val sentInviteList = intent.getSerializableExtra("SENT_INVITE_LIST") as ArrayList<Friend>?
        receivedInviteList = (intent.getSerializableExtra("RECEIVED_INVITE_LIST") as ArrayList<Friend>?)!!

        //Nhan cac list
        if (userProfile != null) {
            homeViewModel.getUserInfo(userProfile.signInKey, userProfile.userId, userProfile.userId)
        }
        homeViewModel.userInfoResponse.observe(this) { response ->
            handleUserInfoResponse(response)
        }

        friendsListadapter = FriendsListAdapter(friendList, object: FriendsListInterface{
            override fun OnClickRemoveFriend(position: Int) {
                if (userProfile != null) {
                    homeViewModel.removeFriend(userProfile.signInKey, userProfile.userId, friendList[position].id)
                }
            }
        })
        binding.rvFriendsList.adapter = friendsListadapter

        binding.rvFriendsList.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false)


        friendRequestsadapter = FriendRequestsAdapter(receivedInviteList, object: FriendRequestsInterface{
            override fun OnClickAcceptFriend(position: Int) {
                if (userProfile != null) {
                    homeViewModel.acceptInvite(userProfile.signInKey, userProfile.userId, receivedInviteList[position].id)
                }
            }
        })
        binding.rvFriendRequest.adapter = friendRequestsadapter

        binding.rvFriendRequest.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false)

        // Lang nghe ket qua tra ve cua accept
        homeViewModel.acceptInviteResponse.observe(this) { response ->
            handleAcceptInviteResponse(response)
        }

        // Lang nghe ket qua tra ve cua remove
        homeViewModel.removeFriendResponse.observe(this) { response ->
            handleRemoveFriendResponse(response)
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("USER_PROFILE", userProfile)
                putExtra("FRIEND_LIST", friendList)
                putExtra("SENT_INVITE_LIST", sentInviteList)
                putExtra("RECEIVED_INVITE_LIST", receivedInviteList)
            }
            startActivity(intent)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleUserInfoResponse(response: Home.UserInfoResponse) {
        when (response.status) {
            200 -> {
                val metadata = response.metadata ?: return
                // Xử lý metadata
                Log.d("UserInfo", "Thông tin người dùng: ${metadata.fullname.firstname} ${metadata.fullname.lastname}")

                // Cap nhat friend list
                if (metadata.friendList != null) {
                    friendList.clear()
                    for (friendData in metadata.friendList) {
                        val id = friendData.id
                        val firstname = friendData.name.firstname
                        val lastname = friendData.name.lastname
                        val profileImageUrl = friendData.profileImageUrl

                        val friend = Friend(id, Fullname(firstname, lastname), profileImageUrl)
                        friendList.add(friend)
                    }
                }

                // Cap nhat received list
                if (metadata.receivedInviteList != null) {
                    receivedInviteList.clear()
                    for (friendData in metadata.receivedInviteList!!) {
                        val id = friendData.id
                        val firstname = friendData.name.firstname
                        val lastname = friendData.name.lastname
                        val profileImageUrl = friendData.profileImageUrl

                        val friend = Friend(id, Fullname(firstname, lastname), profileImageUrl)
                        receivedInviteList.add(friend)
                    }
                }

                friendsListadapter.notifyDataSetChanged()
                friendRequestsadapter.notifyDataSetChanged()
            }
            400 -> {
                // Xử lý các trạng thái lỗi khác
                Log.e("UserInfo", "Lỗi khi lấy thông tin người dùng: ${response.message}")
            }
            else -> {
                // Xử lý lỗi không xác định
                Log.e("UserInfo", "Lỗi không xác định: ${response.message}")
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleRemoveFriendResponse(response: Home.RemoveFriendResponse) {
        when (response.status) {
            200 -> {
                val metadata = response.metadata ?: return
                // Xử lý metadata
                Log.d("RemoveFriend", "Bạn bè đã được xóa: ${metadata.friendList}")

                // Cap nhat friend list
                friendList.clear()
                for (friendData in metadata.friendList) {
                    val id = friendData.id
                    val firstname = friendData.name.firstname
                    val lastname = friendData.name.lastname
                    val profileImageUrl = friendData.profileImageUrl

                    val friend = Friend(id, Fullname(firstname, lastname), profileImageUrl)
                    friendList.add(friend)
                }

                // Cap nhat received list
                receivedInviteList.clear()
                for (friendData in metadata.receivedInviteList) {
                    val id = friendData.id
                    val firstname = friendData.name.firstname
                    val lastname = friendData.name.lastname
                    val profileImageUrl = friendData.profileImageUrl

                    val friend = Friend(id, Fullname(firstname, lastname), profileImageUrl)
                    receivedInviteList.add(friend)
                }

                friendsListadapter.notifyDataSetChanged()
                friendRequestsadapter.notifyDataSetChanged()
            }
            400 -> {
                // Xử lý các trạng thái lỗi khác
                Log.e("RemoveFriend", "Lỗi khi xóa bạn bè: ${response.message}")
            }
            else -> {
                // Xử lý lỗi không xác định
                Log.e("RemoveFriend", "Lỗi không xác định: ${response.message}")
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleAcceptInviteResponse(response: Home.AcceptInviteResponse) {
        when (response.status) {
            200 -> {
                val metadata = response.metadata ?: return
                // Xử lý metadata
                Log.d("AcceptInvite", "Lời mời kết bạn đã được chấp nhận: ${metadata.friendList}")

                // Cap nhat friend list
                friendList.clear()
                for (friendData in metadata.friendList) {
                    val id = friendData.id
                    val firstname = friendData.name.firstname
                    val lastname = friendData.name.lastname
                    val profileImageUrl = friendData.profileImageUrl

                    val friend = Friend(id, Fullname(firstname, lastname), profileImageUrl)
                    friendList.add(friend)
                }

                // Cap nhat received list
                receivedInviteList.clear()
                for (friendData in metadata.receivedInviteList) {
                    val id = friendData.id
                    val firstname = friendData.name.firstname
                    val lastname = friendData.name.lastname
                    val profileImageUrl = friendData.profileImageUrl

                    val friend = Friend(id, Fullname(firstname, lastname), profileImageUrl)
                    receivedInviteList.add(friend)
                }

                friendsListadapter.notifyDataSetChanged()
                friendRequestsadapter.notifyDataSetChanged()
            }
            400 -> {
                // Xử lý các trạng thái lỗi khác
                Log.e("AcceptInvite", "Lỗi khi chấp nhận lời mời kết bạn: ${response.message}")
            }
            else -> {
                // Xử lý lỗi không xác định
                Log.e("AcceptInvite", "Lỗi không xác định: ${response.message}")
            }
        }
    }
}