package com.cllaque.personams.Service.Impl;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cllaque.personams.DTO.ClienteResp;
import com.cllaque.personams.DTO.CrearClienteReq;
import com.cllaque.personams.Domain.Cliente;
import com.cllaque.personams.Excepcion.ConflictException;
import com.cllaque.personams.Excepcion.GenericException;
import com.cllaque.personams.Excepcion.NotFoundException;
import com.cllaque.personams.Repository.ClienteRepository;
import com.cllaque.personams.Service.ClienteService;

import reactor.core.publisher.Mono;

@Service
public class ClienteServiceImpl implements ClienteService{

    private ClienteRepository clienteRepository;
    private PasswordEncoder encoder;

    public ClienteServiceImpl(ClienteRepository clienteRepository, PasswordEncoder encoder){
        this.clienteRepository = clienteRepository;
        this.encoder = encoder;
    }

    @Override
    public Mono<ClienteResp> crearCliente(CrearClienteReq req) {
        return Mono.fromCallable(()->this.clienteRepository.findById(req.getDni()))
        .flatMap(opt->{
            if(opt.isPresent()){
                return Mono.error(new ConflictException("El cliente ya esta registrado"));
            }else{
                var cliente = new Cliente();
                cliente.setDni(req.getDni());
                cliente.setContrasena(encoder.encode(req.getContrasena()));
                cliente.setClienteId(UUID.randomUUID().toString());
                cliente.setEdad(req.getEdad());
                cliente.setEstado(true);
                cliente.setGenero(req.getGenero());
                cliente.setNombre(req.getNombre());
                cliente.setTelefono(req.getTelefono());
                cliente.setDireccion(req.getDireccion());
                var clienteGuardado = this.clienteRepository.save(cliente);

                var clienteResp = new ClienteResp();
                clienteResp.setDni(clienteGuardado.getDni());
                clienteResp.setDireccion(clienteGuardado.getDireccion());
                clienteResp.setTelefono(clienteGuardado.getTelefono());
                clienteResp.setNombre(clienteGuardado.getNombre());
                clienteResp.setEdad(clienteGuardado.getEdad());
                clienteResp.setEstado(clienteGuardado.getEstado());
                clienteResp.setGenero(clienteGuardado.getGenero());
                clienteResp.setClienteId(clienteGuardado.getClienteId());

                return Mono.just(clienteResp);
            }
        });
    }

    @Override
    public Mono<Void> actualizarCliente(CrearClienteReq req) {
        return Mono.fromCallable(()->this.clienteRepository.existsById(req.getDni()))
        .flatMap(exists->{
            if(!exists){
                return Mono.error(new NotFoundException("El cliente no existe"));
            }else{
                var contrasenaEncriptada = req.getContrasena() != null? encoder.encode(req.getContrasena()) : null;

                return Mono.fromRunnable(()->this.clienteRepository.actualizarCliente(req.getDni(), req.getEdad(), 
                    req.getEstado(), contrasenaEncriptada, req.getDireccion(), req.getTelefono(), req.getNombre(), 
                    req.getGenero()))
                .onErrorResume(error -> {
                    error.printStackTrace();
                    return Mono.error(new GenericException("No se pudo eliminar al cliente", error));
                });
            }
        }).then();
    }

    @Override
    public Mono<ClienteResp> obtenerCliente(String dni) {
        return Mono.fromCallable(()->this.clienteRepository.findById(dni))
        .flatMap(opt->{
            if(!opt.isPresent()){
                return Mono.error(new NotFoundException("El cliente no existe"));
            }else{
                var cliente = opt.get();
                var clienteResp = new ClienteResp();
                clienteResp.setDni(cliente.getDni());
                clienteResp.setDireccion(cliente.getDireccion());
                clienteResp.setTelefono(cliente.getTelefono());
                clienteResp.setNombre(cliente.getNombre());
                clienteResp.setEdad(cliente.getEdad());
                clienteResp.setEstado(cliente.getEstado());
                clienteResp.setGenero(cliente.getGenero());
                clienteResp.setClienteId(cliente.getClienteId());

                return Mono.just(clienteResp);
            }
        });
    }

    @Override
    public Mono<Void> eliminarCliente(String dni) {
        return Mono.fromCallable(()->this.clienteRepository.existsById(dni))
        .flatMap(exists -> {
            if (!exists) {
                return Mono.error(new NotFoundException("El cliente no existe"));
            } else {
                return Mono.fromRunnable(()->this.clienteRepository.deleteById(dni))
                .onErrorResume(error -> {
                    return Mono.error(new GenericException("No se pudo eliminar al cliente", error));
                });
            }
        }).then();
    }
    
}