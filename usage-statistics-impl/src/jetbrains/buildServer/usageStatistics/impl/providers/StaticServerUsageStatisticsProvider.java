/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.usageStatistics.impl.providers;

import jetbrains.buildServer.groups.UserGroupManager;
import jetbrains.buildServer.serverSide.BuildAgentManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import org.jetbrains.annotations.NotNull;

public class StaticServerUsageStatisticsProvider extends BaseUsageStatisticsProvider {
  @NotNull private static final String ourGroupName = "Static Server Information";
  @NotNull private final SBuildServer myServer;
  @NotNull private final UserGroupManager myUserGroupManager;

  public StaticServerUsageStatisticsProvider(@NotNull final SBuildServer server, @NotNull final UserGroupManager userGroupManager) {
    super(server);
    myServer = server;
    myUserGroupManager = userGroupManager;
  }

  public void accept(@NotNull final UsageStatisticsPublisher publisher) {
    publishNumberOfAgents(publisher);
    publishNumberOfBuildTypes(publisher);
    publishNumberOfProjects(publisher);
    publishNumberOfUserGroups(publisher);
    publishNumberOfUsers(publisher);
    publishNumberOfVcsRoots(publisher);
  }

  private void publishNumberOfAgents(@NotNull final UsageStatisticsPublisher publisher) {
    final BuildAgentManager buildAgentManager = myServer.getBuildAgentManager();
    final int agentNumber = buildAgentManager.getRegisteredAgents(true).size() + buildAgentManager.getUnregisteredAgents().size();
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.agentNumber", "Number of agents", agentNumber, null, ourGroupName);
  }

  private void publishNumberOfBuildTypes(@NotNull final UsageStatisticsPublisher publisher) {
    final int buildTypeNumber = myServer.getProjectManager().getNumberOfBuildTypes();
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.buildTypeNumber", "Number of build configurations", buildTypeNumber, null, ourGroupName);
  }

  private void publishNumberOfProjects(@NotNull final UsageStatisticsPublisher publisher) {
    final int projectNumber = myServer.getProjectManager().getNumberOfProjects();
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.projectNumber", "Number of projects", projectNumber, null, ourGroupName);
  }

  private void publishNumberOfUserGroups(@NotNull final UsageStatisticsPublisher publisher) {
    final int userGroupNumber = myUserGroupManager.getUserGroups().size();
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.userGroupNumber", "Number of user groups", userGroupNumber, null, ourGroupName);
  }

  private void publishNumberOfUsers(@NotNull final UsageStatisticsPublisher publisher) {
    final int userNumber = myServer.getUserModel().getNumberOfRegisteredUsers();
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.userNumber", "Number of users", userNumber, null, ourGroupName);
  }

  private void publishNumberOfVcsRoots(@NotNull final UsageStatisticsPublisher publisher) {
    final int vcsRootNumber = myServer.getVcsManager().getAllRegisteredVcsRoots().size();
    publisher.publishStatistic("jetbrains.buildServer.usageStatistics.vcsRootNumber", "Number of VCS roots", vcsRootNumber, null, ourGroupName);
  }
}