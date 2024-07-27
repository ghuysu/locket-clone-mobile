package com.myproject.locket_clone.view.feed

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.myproject.locket_clone.databinding.ActivityFeedBinding
import com.myproject.locket_clone.databinding.FeedItemBinding
import com.myproject.locket_clone.model.Feed
import com.myproject.locket_clone.model.Friend
import com.myproject.locket_clone.model.Fullname
import com.myproject.locket_clone.model.GetCertainFeedsResponse
import com.myproject.locket_clone.model.ReactFeedResponse
import com.myproject.locket_clone.model.Reaction
import com.myproject.locket_clone.model.ReactionStatistic
import com.myproject.locket_clone.model.UserProfile
import com.myproject.locket_clone.recycler_view.AllFriendsAdapter
import com.myproject.locket_clone.recycler_view.AllFriendsInterface
import com.myproject.locket_clone.recycler_view.FeedAdapter
import com.myproject.locket_clone.recycler_view.FeedInterface
import com.myproject.locket_clone.repository.Repository
import com.myproject.locket_clone.viewmodel.feed.FeedViewModel
import com.myproject.locket_clone.viewmodel.feed.FeedViewModelFactory

class FeedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedBinding
    private lateinit var homeBinding: FeedItemBinding
    private lateinit var feedAdapter: FeedAdapter
    private var feedList = ArrayList<Feed>()
    private var friendList = ArrayList<Friend>()
    private lateinit var allFriendsadapter: AllFriendsAdapter
    private lateinit var feedViewModel: FeedViewModel
    private var isClickAllFriends = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        homeBinding = FeedItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Khoi tao ban dau
        val repository by lazy { Repository() }
        val viewModelFactory by lazy { FeedViewModelFactory(repository) }
        feedViewModel = ViewModelProvider(this, viewModelFactory).get(
            FeedViewModel::class.java
        )

        //Nhan du lieu tu HomeActivity
        val userProfile: UserProfile? = intent.getSerializableExtra("USER_PROFILE") as? UserProfile
        friendList = intent.getSerializableExtra("FRIEND_LIST") as ArrayList<Friend>
        val sentInviteList = intent.getSerializableExtra("SENT_INVITE_LIST") as ArrayList<Friend>
        val receivedInviteList =
            intent.getSerializableExtra("RECEIVED_INVITE_LIST") as ArrayList<Friend>

        //Lay tat ca feed cho user
        if (userProfile != null) {
            for (u in friendList) {
                feedViewModel.getCertainFeeds(userProfile.signInKey, userProfile.userId, u.id, 1)
            }
        }

        //Hien thi feeds
        feedAdapter = FeedAdapter(feedList, object : FeedInterface {
            override fun onClickHeart(position: Int) {
                if (userProfile != null) {
                    feedViewModel.reactFeed(userProfile.signInKey, userProfile.userId, feedList[position]._id, "love")
                }
            }

            override fun onClickHaha(position: Int) {
                if (userProfile != null) {
                    feedViewModel.reactFeed(userProfile.signInKey, userProfile.userId, feedList[position]._id, "haha")
                }
            }

            override fun onClickLike(position: Int) {
                if (userProfile != null) {
                    feedViewModel.reactFeed(userProfile.signInKey, userProfile.userId, feedList[position]._id, "love")
                }
            }

            override fun onClickWow(position: Int) {
                if (userProfile != null) {
                    feedViewModel.reactFeed(userProfile.signInKey, userProfile.userId, feedList[position]._id, "wow")
                }
            }

            override fun onClickAngry(position: Int) {
                if (userProfile != null) {
                    feedViewModel.reactFeed(userProfile.signInKey, userProfile.userId, feedList[position]._id, "angry")
                }
            }

            override fun onClickSad(position: Int) {
                if (userProfile != null) {
                    feedViewModel.reactFeed(userProfile.signInKey, userProfile.userId, feedList[position]._id, "sad")
                }
            }

            override fun onClickUserProfile(position: Int) {
                TODO("Not yet implemented")
            }

            override fun onClickAllFriends(position: Int) {
                if (isClickAllFriends) {
                    isClickAllFriends = false
                    homeBinding.rvAllFriends.visibility = View.GONE
                    feedList.clear()
                    //Lay tat ca feed cho user
                    if (userProfile != null) {
                        for (u in friendList) {
                            feedViewModel.getCertainFeeds(userProfile.signInKey, userProfile.userId, u.id, 1)
                        }
                    }
                } else {
                    isClickAllFriends = true
                    homeBinding.rvAllFriends.visibility = View.VISIBLE
                }

            }

            override fun onClickSearchUser(position: Int) {
                TODO("Not yet implemented")
            }

            override fun onClickGrid(position: Int) {
                val intent = Intent(this@FeedActivity, FeedInGridActivity::class.java).apply {
                    putExtra("USER_PROFILE", userProfile)
                    putExtra("FRIEND_LIST", friendList)
                    putExtra("SENT_INVITE_LIST", sentInviteList)
                    putExtra("RECEIVED_INVITE_LIST", receivedInviteList)
                    putExtra("FEED_LIST", feedList)
                }
                startActivity(intent)
            }

            override fun onClickMore(position: Int) {
                TODO("Not yet implemented")
            }

        })
        binding.rvFeed.adapter = feedAdapter

        binding.rvFeed.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        // Sử dụng SnapHelper để tạo hiệu ứng lướt
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvFeed)

        //Gan cho danh sach ban be
        homeBinding.rvAllFriends.visibility = View.VISIBLE
        allFriendsadapter = AllFriendsAdapter(friendList, object: AllFriendsInterface {
            override fun onClickFriend(position: Int) {
                feedList.clear()
                if (userProfile != null) {
                    feedViewModel.getCertainFeeds(userProfile.signInKey, userProfile.userId, friendList[position].id, 1)
                }
            }

        })
        homeBinding.rvAllFriends.adapter = allFriendsadapter

        homeBinding.rvAllFriends.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false)

        //Xu ly ket qua tra ve tu server
        //Lay cac bai dang
        feedViewModel.getCertainFeedsResponse.observe(this) { response ->
            handleGetCertainFeedsResponse(response)
        }
        //Bay to cam xuc
        feedViewModel.reactFeedResponse.observe(this) { response ->
            handleReactFeedResponse(response)
        }
    }

    private fun handleReactFeedResponse(response: ReactFeedResponse) {
        when (response.status) {
            200 -> {
                val feedMetadata = response.metadata ?: return
                Log.d("ReactFeed", "Feed reacted successfully: ${feedMetadata.description}")
            }
            400 -> {
                // Xử lý các trạng thái lỗi khác
                Log.e("ReactFeed", "Lỗi khi phản hồi feed: ${response.message}")
            }
            else -> {
                // Xử lý lỗi không xác định
                Log.e("ReactFeed", "Lỗi không xác định: ${response.message}")
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleGetCertainFeedsResponse(response: GetCertainFeedsResponse) {
        when (response.status) {
            200 -> {
                val feeds = response.metadata ?: return
                // Xử lý danh sách feed
                feeds.forEach { feed ->
                    val feedId = feed._id
                    val userId = feed.userId
                    val description = feed.description
                    val profileImageUrl = feed.imageUrl
                    val createdAt = feedViewModel.formatDateString(feed.createdAt)
                    val updatedAt = feed.updatedAt
                    val v = feed.__v
                    val reactionStatistic = ReactionStatistic(
                        feed.reactionStatistic.angry,
                        feed.reactionStatistic.haha,
                        feed.reactionStatistic.like,
                        feed.reactionStatistic.love,
                        feed.reactionStatistic.sad,
                        feed.reactionStatistic.wow
                    )

                    val reactions = ArrayList<Reaction>()
                    for (r in feed.reactions) {
                        val userIdReaction = r.userId
                        val reactionFullname = Fullname(r.fullname.firstname, r.fullname.lastname)
                        val reactionImageUrl = r.profileImageUrl
                        val reactionIcon = r.icon
                        reactions.add(
                            Reaction(
                                userIdReaction,
                                reactionFullname,
                                reactionImageUrl,
                                reactionIcon
                            )
                        )
                    }

                    val visibility = ArrayList<String>()
                    if (feed.visibility is List<*>) {
                        (feed.visibility as List<*>).forEach {
                            visibility.add(it.toString())
                        }
                    } else if (feed.visibility is String) {
                        visibility.add(feed.visibility as String)
                    }

                    var name: String = ""
                    for (n in friendList) {
                        if (n.id == userId) {
                            name = n.name.firstname + " " + n.name.lastname
                            break
                        }
                    }

                    feedList.add(
                        Feed(
                            userId,
                            description,
                            profileImageUrl,
                            visibility,
                            feedId,
                            reactions,
                            reactionStatistic,
                            createdAt,
                            updatedAt,
                            v,
                            name
                        )
                    )

                    feedAdapter.notifyDataSetChanged()
                }
            }

            400 -> {
                // Xử lý các trạng thái lỗi khác
                Log.e("GetCertainFeeds", "Lỗi khi lấy feed: ${response.message}")
            }

            else -> {
                // Xử lý lỗi không xác định
                Log.e("GetCertainFeeds", "Lỗi không xác định: ${response.message}")
            }
        }
    }


}