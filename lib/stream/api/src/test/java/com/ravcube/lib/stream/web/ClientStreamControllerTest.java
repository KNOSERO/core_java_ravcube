package com.ravcube.lib.stream.web;

import com.ravcube.lib.stream.application.ClientStreamAccessDeniedException;
import com.ravcube.lib.stream.application.ClientStreamLimitExceededException;
import com.ravcube.lib.stream.application.ClientStreamService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ClientStreamControllerTest {

    private final ClientStreamService service = mock(ClientStreamService.class);
    private final SseEmitter emitter = new SseEmitter();
    private final MockMvc mockMvc = standaloneSetup(new ClientStreamController(service))
            .addPlaceholderValue("ravcube.stream.path", "/streams")
            .build();

    @AfterEach
    void closeStream() {
        emitter.complete();
    }

    @Test
    void singleResourceRequestOpensStream() throws Exception {
        when(service.subscribe("claims", "1")).thenReturn(emitter);

        mockMvc.perform(get("/streams/claims/1"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        verify(service).subscribe("claims", "1");
    }

    @Test
    void selectedResourcesRequestOpensStream() throws Exception {
        final List<String> resourceIds = List.of("1", "2");
        when(service.subscribe("claims", resourceIds)).thenReturn(emitter);

        mockMvc.perform(get("/streams/claims")
                        .param("ids", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        verify(service).subscribe("claims", resourceIds);
    }

    @Test
    void invalidIdsReturnBadRequest() throws Exception {
        when(service.subscribe("claims", List.of(" ")))
                .thenThrow(new IllegalArgumentException("resourceId must not be blank"));

        mockMvc.perform(get("/streams/claims").param("ids", " "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthorizedResourceReturnsForbidden() throws Exception {
        when(service.subscribe("claims", "1"))
                .thenThrow(new ClientStreamAccessDeniedException("claims", "1"));

        mockMvc.perform(get("/streams/claims/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void subscriptionLimitReturnsTooManyRequests() throws Exception {
        when(service.subscribe("claims", List.of("1")))
                .thenThrow(new ClientStreamLimitExceededException("limit reached"));

        mockMvc.perform(get("/streams/claims").param("ids", "1"))
                .andExpect(status().isTooManyRequests());
    }
}
