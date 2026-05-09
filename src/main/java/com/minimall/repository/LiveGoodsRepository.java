package com.minimall.repository;

import com.minimall.model.LiveGoods;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LiveGoodsRepository extends JpaRepository<LiveGoods, String> {
    List<LiveGoods> findByLiveRoomIdOrderBySortOrderAsc(String liveRoomId);
}