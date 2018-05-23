package edu.miis.Dao;

import edu.miis.Entities.UserBean;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.List;

//@Repository("userDao")
@Component
public interface UserRepository extends JpaRepository<UserBean,Long> {


    @Query(value="select u from edu.miis.Entities.UserBean u where u.username=:usernamee")
    List<UserBean> usernameVerify(@Param("usernamee") String usernamee);

    @Query(value="select u from edu.miis.Entities.UserBean u where u.username=:usernamee")
    List<UserBean> selectByUsername(@Param("usernamee") String usernamee);
@Query(value="select u from edu.miis.Entities.UserBean u")
    List<UserBean> selectAll();

@Query(value="select u from edu.miis.Entities.UserBean u where u.id=:uid")
    List<UserBean> load(@Param("uid") Long id);

@Query(value="select * from user_info where username like concat('%',:name,'%')",nativeQuery = true)
    List<UserBean> searchByName(@Param("name") String name);
}
