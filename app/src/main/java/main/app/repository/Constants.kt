package main.app.repository

import main.app.dataModel.Post

object Constants {
    fun getPostData(): ArrayList<Post>{
        val postList = ArrayList<Post>()

        val post1 = Post(1, 1, "Post 1", "This is post 1")
        postList.add(post1)
        val post2 = Post(2, 2, "Post 2", "This is post 2")
        postList.add(post2)
        val post3 = Post(3, 3, "Post 3", "This is post 3")
        postList.add(post3)
        val post4 = Post(4, 4, "Post 4", "This is post 4")
        postList.add(post4)
        val post5 = Post(5, 5, "Post 5", "This is post 5")
        postList.add(post5)
        val post6 = Post(6, 6, "Post 6", "This is post 6")
        postList.add(post6)
        val post7 = Post(7, 7, "Post 7", "This is post 7")
        postList.add(post7)
        val post8 = Post(8, 8, "Post 8", "This is post 8")
        postList.add(post8)
        val post9 = Post(9, 9, "Post 9", "This is post 9")
        postList.add(post9)
        val post10 = Post(10, 10, "Post 10", "This is post 10")
        postList.add(post10)

        return postList
    }
}