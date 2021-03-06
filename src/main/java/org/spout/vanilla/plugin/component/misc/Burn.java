/*
 * This file is part of Vanilla.
 *
 * Copyright (c) 2011-2012, Spout LLC <http://www.spout.org/>
 * Vanilla is licensed under the Spout License Version 1.
 *
 * Vanilla is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Vanilla is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://spout.in/licensev1> for the full license, including
 * the MIT license.
 */
package org.spout.vanilla.plugin.component.misc;

import org.spout.api.geo.discrete.Point;

import org.spout.vanilla.api.component.misc.BurnComponent;
import org.spout.vanilla.api.data.VanillaData;
import org.spout.vanilla.api.event.cause.DamageCause.DamageType;
import org.spout.vanilla.api.event.cause.NullDamageCause;

import org.spout.vanilla.plugin.component.living.Living;
import org.spout.vanilla.plugin.component.world.VanillaSky;

/**
 * Component handling a entity being on fire.
 */
public class Burn extends BurnComponent {
	private float internalTimer = 0.0f, rainTimer = 0f;
	private Health health;
	private Living living;

	@Override
	public void onAttached() {
		health = getOwner().add(Health.class);
		living = getOwner().get(Living.class);
	}

	@Override
	public boolean canTick() {
		return getFireTick() >= 0 && !health.isDead();
	}

	@Override
	public void onTick(float dt) {
		VanillaSky sky = VanillaSky.getSky(getOwner().getWorld());
		if (sky.hasWeather()) {
			Point point = getOwner().getScene().getPosition();
			if (sky.getWeatherSimulator().isRainingAt((int) point.getX(), (int) point.getY(), (int) point.getZ(), false)) {
				rainTimer += dt;
			} else {
				rainTimer = 0f;
			}
			if (rainTimer >= 2.0f) {
				setFireTick(0f);
				setFireHurting(false);
				rainTimer = 0f;
			}
		}

		living.sendMetaData();
		if (isFireHurting()) {
			if (internalTimer >= 1.0f) {
				health.damage(1, new NullDamageCause(DamageType.BURN));
				internalTimer = 0;
			}
		}
		setFireTick(getFireTick() - dt);
		if (getFireTick() <= 0) {
			living.sendMetaData();
		}
		internalTimer += dt;
	}

	/**
	 * Sets the entity on fire.
	 * @param time The amount of time in seconds the entity should be on fire.
	 * @param hurt True if the fire should hurt else false.
	 */
	@Override
	public void setOnFire(float time, boolean hurt) {
		setFireTick(time);
		setFireHurting(hurt);
		living.sendMetaData();
	}

	/**
	 * Set the firetick value. Any value higher than 0 will put the entity on fire.
	 * @param fireTick The fire tick amount.
	 */
	private void setFireTick(float fireTick) {
		getOwner().getData().put(VanillaData.FIRE_TICK, fireTick);
	}

	/**
	 * Sets if the fire should hurt or not. Maybe we just want to be warm? ^^
	 * @param fireHurt True if the fire should hurt else false.
	 */
	private void setFireHurting(boolean fireHurt) {
		getOwner().getData().put(VanillaData.FIRE_HURT, fireHurt);
	}
}
