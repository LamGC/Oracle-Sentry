package net.lamgc.oracle.sentry.oci.account;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.identity.model.User;
import com.oracle.bmc.identity.requests.GetUserRequest;

import java.util.Objects;

/**
 * Oracle 云账户.
 * @author LamGC
 */
public final class OracleAccount {

    private final AuthenticationDetailsProvider provider;
    private final OracleClients clients;
    private final User user;

    OracleAccount(AuthenticationDetailsProvider provider) {
        this.provider = Objects.requireNonNull(provider);
        this.clients = new OracleClients(provider);
        this.user = clients.identity().getUser(GetUserRequest.builder()
                .userId(provider.getUserId())
                .build()).getUser();
    }

    /**
     * 获取帐号 Id.
     * @return 返回帐号 OCID.
     */
    public String id() {
        return provider.getUserId();
    }

    /**
     * 获取租户 Id.
     * <p> 该 Id 同时也是根区间 Id.
     * @return 返回租户 Id.
     */
    public String tenantId() {
        return provider.getTenantId();
    }

    /**
     * 获取用户名.
     * @return 返回用户名称.
     */
    public String name() {
        return this.user.getName();
    }

    /**
     * 获取用户说明信息.
     * @return 返回设定的用户说明信息.
     */
    public String description() {
        return this.user.getDescription();
    }

    /**
     * 获取该账户所属的 API 客户端集合.
     * @return 返回该账户所属的甲骨文 API 客户端集.
     */
    public OracleClients clients() {
        return clients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthenticationDetailsProvider thatProvider = ((OracleAccount) o).provider;
        return provider.getUserId().equals(thatProvider.getUserId()) &&
                provider.getTenantId().equals(thatProvider.getTenantId()) &&
                provider.getFingerprint().equals(thatProvider.getFingerprint()) &&
                provider.getKeyId().equals(thatProvider.getKeyId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider.getUserId(), provider.getTenantId(), provider.getFingerprint(), provider.getKeyId());
    }

}
