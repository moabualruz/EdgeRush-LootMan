package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateWishlistSnapshotRequest
import com.edgerush.datasync.api.dto.request.UpdateWishlistSnapshotRequest
import com.edgerush.datasync.api.dto.response.WishlistSnapshotResponse
import com.edgerush.datasync.service.crud.WishlistSnapshotCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.WishlistSnapshotEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/wishlist-snapshots")
@Tag(name = "WishlistSnapshot", description = "Manage wishlistsnapshot entities")
class WishlistSnapshotController(
    service: WishlistSnapshotCrudService
) : BaseCrudController<WishlistSnapshotEntity, Long, CreateWishlistSnapshotRequest, UpdateWishlistSnapshotRequest, WishlistSnapshotResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
