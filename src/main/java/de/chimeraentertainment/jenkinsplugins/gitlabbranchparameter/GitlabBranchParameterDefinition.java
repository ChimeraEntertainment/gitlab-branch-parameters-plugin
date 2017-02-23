package de.chimeraentertainment.jenkinsplugins.gitlabbranchparameter;

import com.dabsquared.gitlabjenkins.connection.GitLabConnectionProperty;
import com.dabsquared.gitlabjenkins.service.GitLabProjectBranchesService;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.StringParameterValue;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

public class GitlabBranchParameterDefinition extends SimpleParameterDefinition
{
	private static final Logger LOGGER = Logger.getLogger(GitlabBranchParameterDefinition.class.getName());

	@Extension
	public static class DescriptorImpl extends ParameterDescriptor {

		public DescriptorImpl() {
			load();
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
			save();
			return super.configure(req, json);
		}

		@Override
		public GitlabBranchParameterDefinition newInstance(StaplerRequest req, JSONObject formData) throws FormException
		{
			return new GitlabBranchParameterDefinition(
				formData.getString("name"),
				formData.getString("description"),
				formData.getString("repository")
			);
		}

		@Override
		public String getDisplayName() {
			return "GitLab Branch Parameter";
		}
	}

	private String repository;

	@DataBoundConstructor
	public GitlabBranchParameterDefinition(String name, String description, String repository)
	{
		super(StringUtils.trim(name), description);
		this.repository = repository;
	}

	public String getRepository()
	{
		return repository;
	}

	public void setRepository(String value)
	{
		this.repository = value;
	}

	@Override
	public ParameterValue createValue(StaplerRequest request, JSONObject jo)
	{
		StringParameterValue value = request.bindJSON(StringParameterValue.class, jo);
		value.setDescription(getDescription());
		return value;
	}

	@Override
	public ParameterValue createValue(String value) throws IllegalArgumentException
	{
		return new StringParameterValue(getName(), value, getDescription());
	}

	public Map<String, String> getBranches() throws IOException
	{
		return computeBranches();
	}

	private Job<?, ?> retrieveCurrentJob() {
		StaplerRequest request = Stapler.getCurrentRequest();
		if (request != null) {
			Ancestor ancestor = request.findAncestor(Job.class);
			return ancestor == null ? null : (Job<?, ?>) ancestor.getObject();
		}
		return null;
	}

	private Map<String, String> computeBranches() throws IOException
	{
		Job<?, ?> project = retrieveCurrentJob();
		GitLabConnectionProperty connectionProperty = project.getProperty(GitLabConnectionProperty.class);
		List<String> branches = GitLabProjectBranchesService.instance().getBranches(connectionProperty.getClient(), repository);
		Map<String, String> map = new TreeMap<>();
		for (String branch: branches) {
			map.put(branch, branch);
		}
		return map;
	}
}
