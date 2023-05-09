package fr.inria.astor.core.solutionsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.main.AstorOutputStatus;

/**
 * Exhaustive Search Engine
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 * 
 */
public abstract class ExhaustiveSearchEngine extends AstorCoreEngine {

	public ExhaustiveSearchEngine(MutationSupporter mutatorExecutor, ProjectRepairFacade projFacade)
			throws JSAPException {
		super(mutatorExecutor, projFacade);
	}

	@Override
	public void startSearch() throws Exception {

		dateInitEvolution = new Date();
		// We don't evolve variants, so the generation is always one.
		generationsExecuted = 1;
		// For each variant (one is enough)
		int maxMinutes = ConfigurationProperties.getPropertyInt("maxtime");
		int num_of_try = 0;
		int v = 0;
		for (ProgramVariant parentVariant : variants) {

			log.debug("\n****\nanalyzing variant #" + (++v) + " out of " + variants.size());
			// We analyze each modifpoint of the variant i.e. suspicious
			// statement
			List<ModificationPoint> modificationPointsToProcess = this.suspiciousNavigationStrategy
					.getSortedModificationPointsList(parentVariant.getModificationPoints());

			for (ModificationPoint modifPoint : modificationPointsToProcess) {
				// We create all operators to apply in the modifpoint
				List<OperatorInstance> operatorInstances = createInstancesOfOperators(
						(SuspiciousModificationPoint) modifPoint);

				if (operatorInstances == null || operatorInstances.isEmpty())
					continue;

				for (OperatorInstance pointOperation : operatorInstances) {

					if (!belowMaxTime(dateInitEvolution, maxMinutes)) {

						this.setOutputStatus(AstorOutputStatus.TIME_OUT);
						log.debug("Max time reached");
						return;
					}

					try {
						log.info("mod_point " + modifPoint);
						log.info("-->op: " + pointOperation);
					} catch (Exception e) {
						log.error(e);
					}
					if(ConfigurationProperties.getProperty("repairmode")!= null &&
							ConfigurationProperties.getPropertyBool("editoperationvalidation")) {
						double suitability_score = measure_suitability(modifPoint, pointOperation);
						if(suitability_score <= ConfigurationProperties.getPropertyDouble("suitabilityThreshold")){
							continue;
						}
					}

					// We validate the variant after applying the operator
					ProgramVariant solutionVariant = variantFactory.createProgramVariantFromAnother(parentVariant,
							generationsExecuted);
					solutionVariant.getOperations().put(generationsExecuted, Arrays.asList(pointOperation));

					applyNewMutationOperationToSpoonElement(pointOperation);

					boolean solution = processCreatedVariant(solutionVariant, generationsExecuted);

					// We undo the operator (for try the next one)
					undoOperationToSpoonElement(pointOperation);

					if (solution) {
						this.solutions.add(solutionVariant);

						this.savePatch(solutionVariant);

						if (ConfigurationProperties.getPropertyBool("stopfirst")) {
							this.setOutputStatus(AstorOutputStatus.STOP_BY_PATCH_FOUND);
							return;
						}
					}

					if (!belowMaxTime(dateInitEvolution, maxMinutes)) {

						this.setOutputStatus(AstorOutputStatus.TIME_OUT);
						log.debug("Max time reached");
						return;
					}
					num_of_try += 1;
					if(num_of_try > ConfigurationProperties.getPropertyInt("maxGeneration")){
						return;
					}
				}
			}
		}
		log.debug("End exhaustive navigation");

		this.setOutputStatus(AstorOutputStatus.EXHAUSTIVE_NAVIGATED);
	}

	/**
	 * @param modificationPoint
	 * @return
	 */
	protected List<OperatorInstance> createInstancesOfOperators(SuspiciousModificationPoint modificationPoint) {
		List<OperatorInstance> ops = new ArrayList<>();
		AstorOperator[] operators = getOperatorSpace().values();
		for (AstorOperator astorOperator : operators) {
			if (astorOperator.canBeAppliedToPoint(modificationPoint)) {
				if (!astorOperator.needIngredient()) {
					List<OperatorInstance> instances = astorOperator.createOperatorInstances(modificationPoint);

					if (instances != null && instances.size() > 0) {
						ops.addAll(instances);
					} else {
						log.error("Ignored operator: The approach has an operator that needs ingredients: "
								+ astorOperator.getClass().getCanonicalName());
					}
				}
			}
		}

		return ops;

	}

}
