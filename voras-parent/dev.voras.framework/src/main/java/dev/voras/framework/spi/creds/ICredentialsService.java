package dev.voras.framework.spi.creds;

import javax.validation.constraints.NotNull;

import dev.voras.ICredentials;

public interface ICredentialsService {

    ICredentials getCredentials(@NotNull String credentialsId) throws CredentialsException;
}