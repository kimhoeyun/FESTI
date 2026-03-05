package com.example.FESTI.presentation.auth;

import com.example.FESTI.application.auth.CompleteProfileUseCase;
import com.example.FESTI.application.auth.MyProfileQueryService;
import com.example.FESTI.application.auth.dto.MyProfile;
import com.example.FESTI.infrastructure.security.UserPrincipal;
import com.example.FESTI.presentation.auth.dto.UpdateCellphoneRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
public class MeController {

    private final MyProfileQueryService myProfileQueryService;
    private final CompleteProfileUseCase completeProfileUseCase;

    public MeController(MyProfileQueryService myProfileQueryService,
                        CompleteProfileUseCase completeProfileUseCase) {
        this.myProfileQueryService = myProfileQueryService;
        this.completeProfileUseCase = completeProfileUseCase;
    }

    @GetMapping
    public ResponseEntity<MyProfile> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(myProfileQueryService.getProfile(principal.userId()));
    }

    @PostMapping("/cellphone")
    public ResponseEntity<Void> updateCellphone(@AuthenticationPrincipal UserPrincipal principal,
                                                @Valid @RequestBody UpdateCellphoneRequest request) {
        completeProfileUseCase.updateCellphone(principal.userId(), request.cellphone());
        return ResponseEntity.noContent().build();
    }
}
