package eu.profinit.opendata.institution.rest;

/**
 * Created by dm on 6/29/16.
 */
public class JSONPackageListStrict {
    private String help;
    private boolean success;
    private JSONPackageListResult result;

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public JSONPackageListResult getResult() {
        return result;
    }

    public void setResult(JSONPackageListResult result) {
        this.result = result;
    }
}
