package restdoc.remoting.common.body;

import restdoc.remoting.common.SpringCloudExposedAPI;

import java.util.List;

public class SpringCloudExposeAPIBody extends BaseExposedAPIBody {

    private List<SpringCloudExposedAPI> apiList;

    public List<SpringCloudExposedAPI> getApiList() {
        return apiList;
    }

    public void setApiList(List<SpringCloudExposedAPI> apiList) {
        this.apiList = apiList;
    }
}