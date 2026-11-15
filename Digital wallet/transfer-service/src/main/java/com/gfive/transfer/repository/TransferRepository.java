
package com.gfive.transfer.repository;

import com.gfive.transfer.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Optional<Transfer> findByReferenceId(String referenceId);

    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

    List<Transfer> findBySenderUserIdOrReceiverUserIdOrderByCreatedAtDesc(
        String senderUserId,
        String receiverUserId
    );
}
