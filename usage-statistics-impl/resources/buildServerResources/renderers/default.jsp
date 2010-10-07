<%--
  ~ Copyright 2000-2010 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><jsp:useBean id="statisticsGroup" scope="request" type="jetbrains.buildServer.usageStatistics.presentation.renderers.DefaultUsageStatisticsGroup"
/><table style="width: 99%;" cellspacing="0">
  <c:forEach var="statistic" items="${statisticsGroup.statistics}">
    <tr class="highlightRow statisticRow">
      <td><c:out value="${statistic.displayName}"/></td>
      <td style="width: 13%"><c:out value="${statistic.formattedValue}"/></td>
    </tr>
  </c:forEach>
</table>
