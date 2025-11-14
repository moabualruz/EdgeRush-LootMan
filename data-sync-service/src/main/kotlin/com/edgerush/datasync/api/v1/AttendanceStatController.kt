package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateAttendanceStatRequest
import com.edgerush.datasync.api.dto.request.UpdateAttendanceStatRequest
import com.edgerush.datasync.api.dto.response.AttendanceStatResponse
import com.edgerush.datasync.service.crud.AttendanceStatCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.AttendanceStatEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/attendance-stats")
@Tag(name = "AttendanceStat", description = "Manage attendancestat entities")
class AttendanceStatController(
    service: AttendanceStatCrudService
) : BaseCrudController<AttendanceStatEntity, Long, CreateAttendanceStatRequest, UpdateAttendanceStatRequest, AttendanceStatResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
