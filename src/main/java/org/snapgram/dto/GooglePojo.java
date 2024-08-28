package org.snapgram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GooglePojo {
    private String sub;
    private String name;
    @JsonProperty("given_name")
    private String givenName;
    @JsonProperty("family_name")
    private String familyName;
    private String email;
    private String picture;
    private String hd;
    @JsonProperty("email_verified")
    private boolean emailVerified;

//sub = 109214574255572637115
//name = Võ Phi Hoàng
//given_name = Võ Phi
//family_name = Hoàng
//picture = https://lh3.googleusercontent.com/a/ACg8ocJJ8B8WkQX-8MVQ42UMCd9-lVoCtE2YMKaPqyFCAfLfCZDs4vc=s96-c
//email = 21130363@st.hcmuaf.edu.vn
//email_verified = true
//hd = st.hcmuaf.edu.vn
}
