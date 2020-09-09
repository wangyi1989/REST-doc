package restdoc.remoting.common;

import io.netty.channel.Channel;
import restdoc.remoting.protocol.LanguageCode;

import java.net.InetSocketAddress;
import java.util.Objects;

public class ApplicationClientInfo {
    private final Channel channel;
    private final String clientId;
    private final LanguageCode language;
    private final int version;
    private volatile long lastUpdateTimestamp = System.currentTimeMillis();
    private String hostname;
    private String osname;
    private String service;
    private ApplicationType applicationType = ApplicationType.REST_WEB;

    public ApplicationClientInfo(Channel channel) {
        this(channel, null, null, 0);
    }

    public ApplicationClientInfo(Channel channel, String clientId, LanguageCode language, int version) {
        this.channel = channel;
        this.clientId = clientId;
        this.language = language;
        this.version = version;

        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        this.hostname = address.getHostName();
    }


    public Channel getChannel() {
        return channel;
    }

    public String getClientId() {
        return clientId;
    }

    public LanguageCode getLanguage() {
        return language;
    }

    public int getVersion() {
        return version;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getOsname() {
        return osname;
    }

    public void setOsname(String osname) {
        this.osname = osname;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationClientInfo that = (ApplicationClientInfo) o;
        return version == that.version &&
                lastUpdateTimestamp == that.lastUpdateTimestamp &&
                Objects.equals(channel, that.channel) &&
                Objects.equals(clientId, that.clientId) &&
                language == that.language &&
                Objects.equals(hostname, that.hostname) &&
                Objects.equals(osname, that.osname) &&
                Objects.equals(service, that.service) &&
                applicationType == that.applicationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, clientId, language, version, lastUpdateTimestamp, hostname, osname, service, applicationType);
    }

    @Override
    public String toString() {
        return "ApplicationClientInfo[" +
                "channel=" + channel +
                ", clientId='" + clientId + '\'' +
                ", language=" + language +
                ", version=" + version +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", hostname='" + hostname + '\'' +
                ", osname='" + osname + '\'' +
                ", service='" + service + '\'' +
                ", applicationType=" + applicationType +
                ']';
    }
}