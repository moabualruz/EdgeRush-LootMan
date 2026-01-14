package com.edgerush.lootman.api.guest

import com.edgerush.lootman.api.common.CrudService

interface GuestCrudService : CrudService<Long, CreateGuestRequest, UpdateGuestRequest, GuestResponse>
