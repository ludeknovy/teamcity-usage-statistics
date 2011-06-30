/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.usageStatistics.presentation;

import jetbrains.buildServer.TeamCityExtension;
import org.jetbrains.annotations.NotNull;

/**
 * Extension point for providing custom usage statistics presentation.
 *
 * @since 6.5.2
 */
public interface UsageStatisticsPresentationProvider extends TeamCityExtension {
  /**
   * This method is called every time when TeamCity collects usage statistics values for some reason.
   * Every call of this method is allways performed after avery call of {@link jetbrains.buildServer.usageStatistics.UsageStatisticsProvider#accept(jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher)}.
   *
   * @param presentationManager presentation manager
   */
  void accept(@NotNull UsageStatisticsPresentationManager presentationManager);
}