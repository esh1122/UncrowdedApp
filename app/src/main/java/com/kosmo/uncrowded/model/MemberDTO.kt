package com.kosmo.uncrowded.model

import kotlinx.serialization.Serializable

@Serializable
data class MemberDTO(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val birthday: String = "",
    val gender: String= "",
    val contact: String= "",
    val addr: String= "",
    val joinDate: String= "",
    val haschild: String= "",
    val gu_code: Int= -1,
    val addrLatitude: String= "",
    val addrLongitude: String= "",
    val token: String= "",
    val inters: MutableList<IntersDTO>?= null, // 관심사항 리스트를 가져온다
    val favorites: MutableList<FavoriteDTO>?= null, // 즐겨찾기 리스트를 가져온다
    val authorities: MutableList<AuthorityDTO>?= null, // 권한 리스트를 가져온다
    val profileImage: String= "", // base64 인코딩 프로필 사진
    val gu_dongs: String= "", // 회원이 사는 행정동 정보
    val gu_name: String= ""
)
