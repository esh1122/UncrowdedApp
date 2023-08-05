package com.kosmo.uncrowded.model

import kotlinx.serialization.Serializable

@Serializable
data class MemberDTO(
    var email: String,
    var pwd: String,
    var name: String = "",
    var birthday: java.util.Date,
    var gender: String= "",
    var contact: String= "",
    var addr: String= "",
    var joinDate: java.util.Date,
    var haschild: String= "",
    var gu_code: String= "",
    var addrLatitude: String= "",
    var addrLongitude: String= "",
    var token: String= "",
    var inters: MutableList<IntersDTO>?, // 관심사항 리스트를 가져온다
    var favorites: MutableList<FavoriteDTO>?, // 즐겨찾기 리스트를 가져온다
    var authorities: MutableList<AuthorityDTO>?, // 권한 리스트를 가져온다
    var profileImage: String= "", // base64 인코딩 프로필 사진
    var gu_dongs: String= "", // 회원이 사는 행정동 정보
    var gu_name: String= ""
)
