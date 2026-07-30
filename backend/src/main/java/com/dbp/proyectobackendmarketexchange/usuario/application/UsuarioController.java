package com.dbp.proyectobackendmarketexchange.usuario.application;

import com.dbp.proyectobackendmarketexchange.usuario.domain.UsuarioService;
import com.dbp.proyectobackendmarketexchange.usuario.dto.AdminUserStatusRequest;
import com.dbp.proyectobackendmarketexchange.usuario.dto.ProfileUpdateRequest;
import com.dbp.proyectobackendmarketexchange.usuario.dto.UsuarioRequestDto;
import com.dbp.proyectobackendmarketexchange.usuario.dto.UsuarioResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> obtenerUsuarioPorId(@PathVariable Long id) {
        UsuarioResponseDto usuario = usuarioService.buscarUsuarioPorId(id);
        return new ResponseEntity<>(usuario, HttpStatus.OK);
    }


    @GetMapping("/listar")
    public ResponseEntity<List<UsuarioResponseDto>> listarUsuarios() {
        List<UsuarioResponseDto> usuarios = usuarioService.listarUsuarios();
        return new ResponseEntity<>(usuarios, HttpStatus.OK);
    }


    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioRequestDto requestDTO) {
        UsuarioResponseDto usuarioActualizado = usuarioService.actualizarUsuario(id, requestDTO);
        return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDto> getMyInfo() {
        UsuarioResponseDto usuarioInfo = usuarioService.getUsuarioOwnInfo();
        return ResponseEntity.ok(usuarioInfo);
    }

    @PutMapping("/me/profile")
    public ResponseEntity<UsuarioResponseDto> updateMyProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.updateMyProfile(request));
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<UsuarioResponseDto> suspendUser(@PathVariable Long id,
                                                          @Valid @RequestBody(required = false) AdminUserStatusRequest request) {
        String reason = request == null ? null : request.getReason();
        return ResponseEntity.ok(usuarioService.suspendUser(id, reason));
    }

    @PutMapping("/{id}/unsuspend")
    public ResponseEntity<UsuarioResponseDto> unsuspendUser(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.unsuspendUser(id));
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<UsuarioResponseDto> blockUser(@PathVariable Long id,
                                                        @Valid @RequestBody(required = false) AdminUserStatusRequest request) {
        String reason = request == null ? null : request.getReason();
        return ResponseEntity.ok(usuarioService.blockUser(id, reason));
    }

    @PutMapping("/{id}/unblock")
    public ResponseEntity<UsuarioResponseDto> unblockUser(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.unblockUser(id));
    }
}
