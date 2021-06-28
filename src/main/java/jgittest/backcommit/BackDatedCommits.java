package jgittest.backcommit;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class BackDatedCommits {

	public static final String localRepositoryPath = "local/repo/path";
	public static final String remoteRepoURI = "https://github.com/username/repo.git";
	public static final String fileName = "time.txt";
	public static final int minCommits = 1;
	public static final int maxCommits = 4;
	public static final String startDateStr = "2020-11-01 06:06:06";
	public static final String endDateStr = "2020-11-30 06:06:06";

	public static void main(String[] args) throws Exception {
		Git git = getGitReference();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date startDate = sdf.parse(startDateStr);
		Date endDate = sdf.parse(endDateStr);
		writeAndCommit(git, startDate, endDate);
		pushToRemote(git);
	}

	private static Git getGitReference() {
		Git git = null;

		try {
			git = Git.open(new File(localRepositoryPath));
		} catch (Exception e) {
			try {
				cloneRepo();
				git = Git.open(new File(localRepositoryPath));
			} catch (Exception ex) {
				System.out.println("Unable to get GIT reference...");
			}
		}

		return git;
	}

	private static void cloneRepo() throws GitAPIException {
		Git.cloneRepository().setURI(remoteRepoURI).setDirectory(new File(localRepositoryPath)).call();
	}

	private static void writeAndCommit(Git git, Date startDate, Date endDate) throws Exception {
		File myFile = new File(git.getRepository().getDirectory().getParent(), fileName);
		if (!myFile.exists()) {
			myFile.createNewFile();
		}
		FileWriter fw = new FileWriter(myFile, false);

		Date selectedDate = startDate;
		Calendar c = Calendar.getInstance();

		while (selectedDate.getTime() >= startDate.getTime() && selectedDate.getTime() <= endDate.getTime()) {
			int numberOfCommits = (int) ((Math.random() * (maxCommits - minCommits)) + minCommits);
			while (numberOfCommits > 0) {
				fw.write(selectedDate.toString() + numberOfCommits);
				fw.flush();
				localCommit(git, selectedDate, numberOfCommits);
				numberOfCommits--;
			}
			c.setTime(selectedDate);
			c.add(Calendar.DATE, 1);
			selectedDate = c.getTime();
		}

		fw.close();
	}

	private static void localCommit(Git git, Date date, int index) throws Exception {
		git.add().addFilepattern(fileName).call();
		git.commit()
				.setCommitter(
						new PersonIdent("User Name", "emailID", date, TimeZone.getDefault()))
				.setMessage("commit message" + date.toString() + index).call();
	}

	private static void pushToRemote(Git git) throws Exception {
		git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider("username", "password")).call();
	}

}
