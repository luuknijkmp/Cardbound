package cardbound;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public final class ModParticles {

    public static final SimpleParticleType ARCANE_SPARK = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE,
            Identifier.fromNamespaceAndPath("cardbound", "arcane_spark"),
            FabricParticleTypes.simple()
    );

    private ModParticles() {
    }

    public static void initialize() {
        // Forces the class to load and register the particle.
    }
}