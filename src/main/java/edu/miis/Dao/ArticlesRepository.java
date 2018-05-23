package edu.miis.Dao;

import edu.miis.Entities.Article;
import edu.miis.Entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Component;


import java.util.Date;
import java.util.List;

//@Repository("articleDao")
@Component
public interface ArticlesRepository extends JpaRepository<Article,Long> {

    @Query(value="select a from edu.miis.Entities.Article a")
    List<Article> loadAll();

    @Query(value="select a from edu.miis.Entities.Article a where a.id=:iid")
    List<Article> loadOne(@Param("iid") Long id);

    //    List<Article> queryByTime(Long fId,Date lastLogin);
    /*
     * 逻辑：
     * 首先，设定登录时间。
     * 查询的时间间隔：用户最后登录时间之前，24小时，所关注之人，发的推。fId 为所关注之人的Id
     *  select a from Article a where a.author.id=:iid and a.insertTime >=
     *
     * */
    @Query(value="select * from Article a where author_id=:iid and a.insert_time>= DATE_SUB(:lastLog,INTERVAL 1 DAY)",nativeQuery = true)
    List<Article> queryByTime(@Param("iid") Long fid, @Param("lastLog")Date lastLogin);


    @Query(value="select c from edu.miis.Entities.Comment c where c.article.id=:aid")
    List<Comment> queryCommentByTime(@Param("aid") Long aid); //,@Param("kaishi") Long entry);

    @Query(value="select * from Article where id=:aid", nativeQuery=true)
    List<Article> queryById(@Param("aid") Long aid);

//
//    @Query(value="select * from Article a where author_id=:iid and a.insert_time>=DATE_SUB() ")
//    List<Article> loadByUserId(Long userId, Date currentDate);
}
