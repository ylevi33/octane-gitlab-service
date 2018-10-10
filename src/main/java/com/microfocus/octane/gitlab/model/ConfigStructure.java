package com.microfocus.octane.gitlab.model;

import javafx.util.Pair;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.MissingRequiredPropertiesException;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

@Component
public class ConfigStructure {

    @Value("${ciserver.identity:#{null}}")
    private String ciServerIdentity;

    @Value("${octane.location:#{null}}")
    private String octaneLocation;

    @Value("${octane.sharedspace:#{null}}")
    private String octaneSharedspace;

    @Value("${octane.username:#{null}}")
    private String octaneUsername;

    @Value("${octane.password:#{null}}")
    private String octanePassword;

    @Value("${gitlab.location:#{null}}")
    private String gitlabLocation;

    @Value("${gitlab.privateToken:#{null}}")
    private String gitlabPrivateToken;

    @Value("${server.baseUrl:#{null}}")
    private String serverBaseUrl;

    @Value("${http.proxyHost:#{null}}")
    private String httpProxyHost;

    @Value("${http.proxyPort:#{null}}")
    private String httpProxyPort;

    @Value("${http.proxyUser:#{null}}")
    private String httpProxyUser;

    @Value("${http.proxyPassword:#{null}}")
    private String httpProxyPassword;

    @Value("${http.nonProxyHosts:#{null}}")
    private String httpNonProxyHosts;

    @Value("${https.proxyHost:#{null}}")
    private String httpsProxyHost;

    @Value("${https.proxyPort:#{null}}")
    private String httpsProxyPort;

    @Value("${https.proxyUser:#{null}}")
    private String httpsProxyUser;

    @Value("${https.proxyPassword:#{null}}")
    private String httpsProxyPassword;

    @Value("${https.nonProxyHosts:#{null}}")
    private String httpsNonProxyHosts;

    @PostConstruct
    public void init() {
        List<Pair<String, Supplier<String>>> mandatoryGetters = new ArrayList<>();
        mandatoryGetters.add(new Pair<>("octaneLocation", this::getOctaneLocation));
        mandatoryGetters.add(new Pair<>("octaneSharedspace", this::getOctaneSharedspace));
        mandatoryGetters.add(new Pair<>("octaneUsername", this::getOctaneUsername));
        mandatoryGetters.add(new Pair<>("octanePassword", this::getOctanePassword));
        mandatoryGetters.add(new Pair<>("gitlabLocation", this::getGitlabLocation));
        mandatoryGetters.add(new Pair<>("gitlabPrivateToken", this::getGitlabPrivateToken));
        Set<String> missingRequiredProperties = new LinkedHashSet();
        mandatoryGetters.stream().forEach(mg -> {
            if (mg.getValue().get() == null || mg.getValue().get().trim().isEmpty()) {
                missingRequiredProperties.add(mg.getKey());
            }
        });

        if (missingRequiredProperties.size() > 0) {
            throw new MissingRequiredPropertiesException() {
                @Override
                public Set<String> getMissingRequiredProperties() {
                    return missingRequiredProperties;
                }
            };
        }
    }

    public String getServerBaseUrl() {
        return serverBaseUrl;
    }

    public String getCiServerIdentity() {
        String val = ciServerIdentity != null ? ciServerIdentity : Hex.encodeHexString(DigestUtils.md5Digest(serverBaseUrl.getBytes()));
        return val.substring(0, Math.min(255, val.length()));
    }

    public String getOctaneLocation() {
        return octaneLocation;
    }

    public String getOctaneSharedspace() {
        return octaneSharedspace;
    }

    public String getOctaneUsername() {
        return octaneUsername;
    }

    public String getOctanePassword() {
        return octanePassword;
    }

    public String getGitlabLocation() {
        return gitlabLocation;
    }

    public String getGitlabPrivateToken() {
        return gitlabPrivateToken;
    }

    public String getProxyField(String protocol, String fieldName) {
        Optional<Field> field = Arrays.stream(this.getClass().getDeclaredFields()).filter(f -> f.getName().toLowerCase().equals(protocol.concat(fieldName).toLowerCase())).findFirst();
        if (!field.isPresent()) {
            throw new IllegalArgumentException(String.format("$s.$s", protocol, fieldName));
        }
        try {
            Object value = field.get().get(this);
            return value != null ? value.toString() : null;
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("$s.$s field in not accessible", protocol, fieldName));
        }
    }
}